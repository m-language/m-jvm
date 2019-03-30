package io.github.m

import io.github.m.List.Nil
import java.nio.file.StandardOpenOption

@Suppress("MemberVisibilityCanBePrivate")
@UseExperimental(ExperimentalUnsignedTypes::class)
object Generator {
    data class Env(val locals: Map<String, Variable.Local>,
                   val globals: Map<String, Variable.Global>,
                   val def: String,
                   val index: UInt) {
        operator fun get(name: String) = locals[name] ?: globals[name]
    }

    data class Result(val operation: Operation,
                      val declarations: Sequence<Declaration>,
                      val env: Env)

    class Failure(message: String) : Exception(message)

    fun mangleLambdaName(name: String, index: UInt) = "${name}_$index"

    fun closures(expr: Expr, env: Env): Set<String> = when (expr) {
        is Expr.Symbol -> when (env[expr.name]) {
            null -> emptySet()
            is Variable.Global -> emptySet()
            is Variable.Local -> setOf(expr.name)
        }
        is Expr.List -> expr.exprs.flatMap { closures(it, env) }.toSet()
    }

    fun generateIdentifierExpr(name: String, env: Env): Result =
            when (val variable = env[name]) {
                is Variable.Local -> Result(Operation.LocalVariable(variable.name, variable.index), nil(), env)
                is Variable.Global -> Result(Operation.GlobalVariable(variable.name, variable.path), nil(), env)
                null -> throw Exception("Could not find $name")
            }

    fun generateNil(env: Env) = Result(Operation.Nil, nil(), env)

    fun generateLambdaExpr(name: String, expr: Expr, env: Env): Result = run {
        val mangledName = mangleLambdaName(env.def, env.index)
        val newEnv = env.copy(index = env.index + 1U)
        val closures = closures(expr, env).asSequence()
        val closureOperations = closures.map { generateIdentifierExpr(it, newEnv).operation }
        val locals = newEnv.locals + closures
                .plus(element = name)
                .withIndex()
                .map { (index, name) -> name to Variable.Local(name.toList, Nat(index.toUInt())) }
                .toMap()
        val result = generateExpr(expr, newEnv.copy(locals = locals, def = mangledName))
        Result(
                Operation.Fn(expr.path.toList, mangledName.toList, name.toList, result.operation, List.valueOf(closureOperations)),
                Declaration.Fn(mangledName.toList, expr.path.toList, List.valueOf(closures.map(String::toList)), result.operation).cons(result.declarations),
                result.env.copy(locals = newEnv.locals, def = newEnv.def, index = newEnv.index)
        )
    }

    fun generateDefExpr(name: String, expr: Expr, env: Env): Result = if (env[name] != null) {
        throw Exception("$name has already been defined")
    } else {
        val newEnv = env.copy(globals = env.globals + (name to Variable.Global(name.toList, expr.path.toList)))
        val result = generateExpr(expr, newEnv.copy(def = name))
        Result(
                Operation.Def(name.toList, expr.path.toList, result.operation),
                Declaration.Def(name.toList, expr.path.toList, result.operation).cons(result.declarations),
                result.env.copy(def = newEnv.def)
        )
    }

    fun generateSymbolExpr(name: String, env: Env): Result = Result(Operation.Symbol(name.toList), nil(), env)

    fun generateApplyExpr(fn: Expr, arg: Expr, env: Env): Result = run {
        val fnResult = generateExpr(fn, env)
        val argResult = generateExpr(arg, fnResult.env)
        Result(
                Operation.Apply(fnResult.operation, argResult.operation),
                fnResult.declarations + argResult.declarations,
                argResult.env
        )
    }

    fun generateListExpr(expr: Expr.List, env: Env): Result = if (expr.exprs.isEmpty()) {
        generateNil(env)
    } else {
        val exprs = expr.exprs
        when ((exprs.first() as? Expr.Symbol)?.name) {
            "def" -> generateDefExpr((exprs[1] as Expr.Symbol).name, exprs[2], env)
            "fn" -> generateLambdaExpr((exprs[1] as Expr.Symbol).name, exprs[2], env)
            "symbol" -> generateSymbolExpr((exprs[1] as Expr.Symbol).name, env)
            else -> generateApplyExpr(exprs[0], exprs[1], env)
        }
    }

    fun generateExpr(expr: Expr, env: Env): Result = try {
        when (expr) {
            is Expr.Symbol -> generateIdentifierExpr(expr.name, env)
            is Expr.List -> generateListExpr(expr, env)
        }.run { copy(operation = Operation.LineNumber(operation, Nat(expr.start.line))) }
    } catch (e: Failure) {
        throw e
    } catch (e: Exception) {
        throw Failure("${e.message} at ${expr.path}.${env.def}(${expr.path.substringAfterLast('/')}.m:${expr.start.line})")
    }

    fun generate(exprs: Sequence<Expr>, env: Env): Result = if (exprs.none()) {
        Result(Operation.Nil, nil(), env)
    } else {
        val car = generateExpr(exprs.car, env)
        val cdr = generate(exprs.cdr, car.env)
        Result(
                cdr.operation,
                car.declarations + cdr.declarations,
                cdr.env
        )
    }

    fun write(bytes: ByteArray, out: File, name: String) {
        val file = out.child("${name.replace('.', '/')}.class").value
        val path = file.toPath()
        file.parentFile.mkdirs()
        if (file.exists()) java.nio.file.Files.delete(path)
        java.nio.file.Files.write(path, bytes, StandardOpenOption.CREATE)
    }

    fun generateProgram(@Suppress("UNUSED_PARAMETER") operation: Operation, declarations: Sequence<Declaration>) =
            declarations
                    .groupBy { it.path.toString }
                    .map { (path, decls) -> path to Backend.run { clazz(path, decls.asSequence()) } }
                    .toMap()

    fun writeProgram(out: File, @Suppress("UNUSED_PARAMETER") operation: Operation, declarations: Sequence<Declaration>) =
            generateProgram(operation, declarations).forEach { (path, bytes) ->
                write(bytes, out, path)
            }

    @Suppress("unused")
    object Definitions {
        @MField("mangle-fn-name")
        @JvmField
        val mangleFnName: Value = Value { name, index ->
            Generator.mangleLambdaName(List.from(name).toString, Nat.from(index).value).toList
        }

        @MField("write-program")
        @JvmField
        val writeProgram: Value = Value { out, operation, declarations ->
            Process {
                Generator.writeProgram(out as File, operation as Operation, List.from(declarations).asSequence().map { it as Declaration })
                Nil
            }
        }

        @MField("debug")
        @JvmField
        val debug: Value = Value { x ->
            println(x)
            x
        }
    }
}