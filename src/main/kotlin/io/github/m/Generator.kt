package io.github.m

import java.nio.file.StandardOpenOption

@Suppress("MemberVisibilityCanBePrivate")
data class Generator(val path: String) {
    sealed class Variable {
        data class Local(val name: List, val index: Nat) : Variable()
        data class Global(val name: List, val path: List) : Variable()
    }

    data class Env(val locals: Map<String, Variable.Local>,
                   val globals: Map<String, Variable.Global>,
                   val def: String,
                   val index: Int) {
        operator fun get(name: String) = locals[name] ?: globals[name]
    }

    data class Result(val operation: Operation,
                      val declarations: Sequence<Declaration>,
                      val env: Env)

    class Failure(message: String) : Exception(message)

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
        val newEnv = env.copy(index = env.index + 1)
        val closures = closures(expr, env).asSequence()
        val closureOperations = closures.map { generateIdentifierExpr(it, newEnv).operation }
        val locals = newEnv.locals + closures
                .plus(element = name)
                .withIndex()
                .map { (index, name) -> name to Variable.Local(Symbol.toList(name), Nat.valueOf(index)) }
                .toMap()
        val result = generateExpr(expr, newEnv.copy(locals = locals, def = mangledName))
        Result(
                Operation.Fn(Symbol.toList(path), Symbol.toList(mangledName), Symbol.toList(name), result.operation, closureOperations.list()),
                Declaration.Fn(Symbol.toList(mangledName), Symbol.toList(path), closures.map { Symbol.toList(it) }.list(), result.operation).cons(result.declarations),
                result.env.copy(locals = newEnv.locals, def = newEnv.def, index = newEnv.index)
        )
    }

    fun generateDefExpr(name: String, expr: Expr, env: Env): Result = if (env[name] != null) {
        throw Exception("$name has already been defined")
    } else {
        val newEnv = env.copy(globals = env.globals + (name to Variable.Global(Symbol.toList(name), Symbol.toList(path))))
        val result = generateExpr(expr, newEnv.copy(def = name))
        Result(
                Operation.Def(Symbol.toList(name), Symbol.toList(path), result.operation),
                Declaration.Def(Symbol.toList(name), Symbol.toList(path), result.operation).cons(result.declarations),
                result.env.copy(def = newEnv.def)
        )
    }

    fun generateSymbolExpr(name: String, env: Env): Result = Result(Operation.Symbol(Symbol.toList(name)), nil(), env)

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
        }
    } catch (e: Failure) {
        throw e
    } catch (e: Exception) {
        throw Failure("${e.message} in ${env.def}")
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

    companion object {
        fun mangleLambdaName(name: String, index: Int) = "${name}_$index"

        fun write(bytes: ByteArray, out: File, name: String) {
            val file = out.child("${name.replace('.', '/')}.class").value
            val path = file.toPath()
            file.parentFile.mkdirs()
            if (file.exists()) java.nio.file.Files.delete(path)
            java.nio.file.Files.write(path, bytes, StandardOpenOption.CREATE)
        }

        fun generateProgram(@Suppress("UNUSED_PARAMETER") operation: Operation, declarations: Sequence<Declaration>) =
                declarations
                        .groupBy { Symbol.toString(it.path) }
                        .map { (path, decls) -> path to Backend.run { clazz(path, decls.asSequence()) } }
                        .toMap()

        fun writeProgram(out: File, @Suppress("UNUSED_PARAMETER") operation: Operation, declarations: Sequence<Declaration>) =
                generateProgram(operation, declarations).forEach { (path, bytes) ->
                    write(bytes, out, path)
                }
    }

    @Suppress("unused")
    object Definitions {
        @MField(name = "mangle-fn-name")
        @JvmField
        val mangleFnName: Value = Value.Impl2("mangle-fn-name") { name, index ->
            Symbol.toList(mangleLambdaName(Symbol.toString(List.from(name)), Nat.from(index).value))
        }

        @MField(name = "jvm-backend")
        @JvmField
        val jvmBackend: Value = Value.Impl3("jvm-backend") { out, operation, declarations ->
            Process {
                writeProgram(out as File, operation as Operation, List.from(declarations).asSequence().map { it as Declaration })
                List.NIL
            }
        }

        @MField(name = "debug")
        @JvmField
        val debug: Value = Value.Impl1("debug") { x ->
            println(x)
            x
        }
    }
}