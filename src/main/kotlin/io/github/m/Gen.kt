package io.github.m

import io.github.m.asm.*
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
object Gen {
    val internals: Map<String, Location.Global> = listOf<java.lang.Class<*>>(
            MAny.Definitions::class.java,
            MBool.Definitions::class.java,
            MList.Definitions::class.java,
            MNat.Definitions::class.java,
            MInt.Definitions::class.java,
            MReal.Definitions::class.java,
            MChar.Definitions::class.java,
            MSymbol.Definitions::class.java,
            MData.Definitions::class.java,
            MError.Definitions::class.java,
            MProcess.Definitions::class.java,
            MFile.Definitions::class.java,
            MUndefined.Definitions::class.java,
            Runtime.Definitions::class.java,
            Gen.Definitions::class.java,
            Expr.Definitions::class.java
    ).flatMap {
        it
                .fields
                .asSequence()
                .filter { field -> field.isAnnotationPresent(MField::class.java) }
                .map { field -> field.getAnnotation(MField::class.java).name to Location.Global(Type.clazz(it), field.name) }
                .toList()
    }.toMap()

    sealed class Location {
        data class Global(val clazz: Type, val name: String) : Location()
        data class Local(val index: Int) : Location()
    }

    data class Env(val clazz: Type,
                   val method: String,
                   val i: Int,
                   val globals: Map<String, Location.Global>,
                   val locals: Map<String, Location.Local>) {
        operator fun get(name: String) = locals.getOrElse(name) { globals[name] }
    }

    data class Decl(val declaration: Declaration, val init: Operation)

    data class Result(val decls: List<Decl>, val op: Operation, val env: Env)

    fun gen(exprs: Sequence<Expr>, `in`: File): Class {
        val type = Type.clazz(QualifiedName.fromPathString(`in`.nameWithoutExtension))
        val env = Env(type, "main", 0, emptyMap(), emptyMap())
        return gen(exprs, emptyList(), block(), env)
    }

    tailrec fun gen(exprs: Sequence<Expr>, decls: List<Decl>, op: Operation, env: Env): Class =
            if (exprs.any()) {
                val (newDecls, newOp, newEnv) = gen(exprs.first(), env)
                gen(exprs.drop(1), decls + newDecls, block(op, newOp), newEnv)
            } else {
                mainClass(env.clazz, decls.map { it.declaration }, block(decls.map { it.init }), op)
            }

    fun gen(expr: Expr, env: Env): Result = try {
        when (expr) {
            is Expr.Identifier -> {
                val location = env[expr.name]
                when (location) {
                    null -> throw Exception("Could not find ${expr.name}")
                    is Location.Global -> Result(emptyList(), pushMGlobal(location.clazz, location.name), env)
                    is Location.Local -> Result(emptyList(), pushMLocal(location.index), env)
                }
            }
            is Expr.List -> {
                val exprs = expr.exprs
                val first = exprs.first()
                when {
                    first is Expr.Identifier && first.name == "if" -> {
                        val (condition, ifTrue, ifFalse) = Triple(exprs[1], exprs[2], exprs[3])
                        val (decls1, conditionOps, env1) = gen(condition, env)
                        val (decls2, ifTrueOps, env2) = gen(ifTrue, env1)
                        val (decls3, ifFalseOps, env3) = gen(ifFalse, env2)
                        Result(decls1 + decls2 + decls3, `if`(conditionOps, ifTrueOps, ifFalseOps), env3)
                    }
                    first is Expr.Identifier && first.name == "symbol" -> {
                        val symbol = (exprs[1] as Expr.Identifier).name
                        Result(emptyList(), pushMSymbol(symbol), env)
                    }
                    first is Expr.Identifier && first.name == "lambda" -> {
                        when (exprs.size) {
                            2 -> gen(Expr.List(listOf(first, Expr.Identifier("unused", expr.line), exprs[1]), expr.line), env)
                            3 -> {
                                val name = (exprs[1] as Expr.Identifier).name
                                val value = exprs[2]
                                val closures = closures(env, value) - name
                                val closureOps = closures.map { gen(Expr.Identifier(it, 1), env).op }
                                val methodName = "${env.method}_${env.i}"
                                val localEnv = env.copy(
                                        locals = (closures + name).asSequence().withIndex().map { it.value to it.index }.toList().toMap().mapValues { (_, index) -> Location.Local(index) },
                                        method = methodName
                                )
                                val (lambdaDecls, lambdaOps, _) = gen(value, localEnv)
                                val ops = lambdaConstructor(env.clazz, methodName, closureOps)
                                val method = Decl(lambdaMethod(methodName, closures, block(lambdaOps, `return`)), block())
                                Result(listOf(method) + lambdaDecls, ops, env.copy(i = env.i + 1))
                            }
                            else -> gen(Expr.List(listOf(first, exprs[1], Expr.List(listOf(first, exprs[2]) + exprs.drop(3), expr.line)), expr.line), env)
                        }
                    }
                    first is Expr.Identifier && first.name == "def" -> {
                        val name = (exprs[1] as Expr.Identifier).name
                        val location = internals[name]
                        when {
                            location != null -> Result(emptyList(), pushMNil, env.copy(globals = env.globals + (name to location)))
                            exprs.size == 2 -> Result(emptyList(), pushMNil, env.copy(globals = env.globals + (name to Location.Global(env.clazz, name))))
                            else -> {
                                val value = exprs[2]
                                val newEnv = env.copy(globals = env.globals + (name to Location.Global(env.clazz, name)))
                                val (valueDecls, valueOps, _) = gen(value, newEnv.copy(method = name))
                                val field = Decl(defDecl(name, newEnv.clazz), initDef(name, valueOps, newEnv.clazz))
                                Result(listOf(field) + valueDecls, pushMNil, newEnv)
                            }
                        }
                    }
                    else -> if (exprs.size == 2) {
                        val fn = exprs[0]
                        val arg = exprs[1]
                        val (decls1, fnOps, env2) = gen(fn, env)
                        val (decls2, argOps, env3) = gen(arg, env2)
                        Result(decls1 + decls2, invoke(fnOps, argOps), env3)
                    } else {
                        val fn = exprs.dropLast(1)
                        val arg = exprs.last()
                        gen(Expr.List(listOf(Expr.List(fn, expr.line), arg), expr.line), env)
                    }
                }
            }
        }.let { it.copy(op = block(lineNumber(expr.line), it.op)) }
    } catch (e: Exception) {
        throw Exception(e.message + "\n\tat ${expr.line}")
                .apply { stackTrace = e.stackTrace }
    }

    fun closures(env: Env, expr: Expr): Set<String> = when (expr) {
        is Expr.Identifier -> if (env.locals.contains(expr.name)) setOf(expr.name) else emptySet()
        is Expr.List -> expr.exprs.flatMap { closures(env, it) }.toSet()
    }

    fun mainClass(
            type: Type,
            decls: Iterable<Declaration>,
            init: Operation,
            operation: Operation
    ): Class {
        val runType = MethodType(
                "run",
                emptyList(),
                Type.void,
                listOf(Type.array(Type.string), Type.clazz(java.lang.Class::class.java)),
                emptySet()
        )
        val main = Method(
                Access().asPublic().asFinal().asStatic(),
                "main",
                emptyList(),
                Type.void,
                listOf(Type.array(Type.string)),
                emptySet(),
                listOf(
                        pushArg(0),
                        pushType(type),
                        invokeStatic(Type.clazz(Runtime::class.java), runType),
                        `return`
                )
        )
        val clinit = Method.staticInit(listOf(init, `return`))
        val run = Method(
                Access().asPublic().asFinal().asStatic(),
                "run",
                emptyList(),
                Type.void,
                emptyList(),
                emptySet(),
                listOf(operation, `return`)
        )
        return Class(
                Access().asPublic().asFinal(),
                type.toQualifiedName(),
                emptyList(),
                Type.`object`,
                emptySet(),
                decls + listOf(clinit, main, run, ClassSource("${type.toQualifiedName().name}.m"))
        )
    }

    fun defDecl(name: String, main: Type) = Field(
            Access().asPublic().asStatic(),
            mAnyType,
            name,
            main
    )

    fun lambdaMethod(name: String, closures: Set<String>, ops: Operation) = Method(
            Access().asPrivate().asStatic().asSynthetic(),
            name,
            emptyList(),
            mAnyType,
            closures.map { mAnyType } + mAnyType,
            emptySet(),
            listOf(ops)
    )

    val mAnyType by lazy { Type.clazz(MAny::class.java) }
    val mFunctionType by lazy { Type.clazz(MFunction::class.java) }

    @Suppress("unused")
    object Definitions {
        @MField("generate")
        @JvmField
        val generate: MAny = MFunction { `in`, out, ast ->
            val exprs = Cast.toList(ast).asSequence().map { it as Expr }
            val gen = gen(exprs, Cast.toFile(`in`).file)
            gen.generate(Cast.toFile(out).file)
            MList.Nil
        }
    }
}