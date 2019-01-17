package io.github.m

import java.nio.file.StandardOpenOption

@Suppress("MemberVisibilityCanBePrivate")
@ExperimentalUnsignedTypes
object Generator {
    data class Env(val exprs: Sequence<Expr>,
                   val locals: Map<String, Variable.Local>,
                   val globals: Map<String, Variable.Global>,
                   val def: String,
                   val index: UInt) {
        operator fun get(name: String) = locals[name] ?: globals[name]
    }

    data class Result(val operation: Operation,
                      val declarations: Sequence<Declaration>,
                      val env: Env)

    val internals: Map<String, Variable.Global> = listOf<java.lang.Class<*>>(
            Bool.Definitions::class.java,
            Char.Definitions::class.java,
            Data.Definitions::class.java,
            Declaration.Definitions::class.java,
            Errors::class.java,
            File.Definitions::class.java,
            Function.Definitions::class.java,
            Generator.Definitions::class.java,
            List.Definitions::class.java,
            Nat.Definitions::class.java,
            Operation.Definitions::class.java,
            Pair.Definitions::class.java,
            Process.Definitions::class.java,
            Runtime::class.java,
            Symbol.Definitions::class.java,
            Variable.Definitions::class.java
    ).flatMap {
        it
                .fields
                .asSequence()
                .filter { field -> field.isAnnotationPresent(MField::class.java) }
                .map { field ->
                    val name = field.getAnnotation(MField::class.java).name
                    val variable = Variable.Global(field.name.toList, it.name.toList)
                    name to variable
                }
                .toList()
    }.toMap()

    fun mangleLambdaName(name: String, index: UInt) = "${name}_$index"

    fun closures(expr: Expr, env: Env): Set<String> = when (expr) {
        is Expr.Identifier -> when (env[expr.name]) {
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
                null -> if (env.exprs.none()) {
                    throw Exception("Could not find $name")
                } else {
                    val next = generateExpr(env.exprs.car, env.copy(exprs = env.exprs.cdr, locals = emptyMap(), def = "", index = 0U))
                    val result = generateIdentifierExpr(name, env.copy(exprs = next.env.exprs, globals = next.env.globals))
                    Result(
                            Operation.Combine(next.operation, result.operation),
                            next.declarations + result.declarations,
                            result.env
                    )
                }
            }

    fun generateNil(env: Env) = Result(Operation.Nil, nil(), env)

    fun generateIfExpr(cond: Expr, `true`: Expr, `false`: Expr, env: Env): Result = run {
        val condResult = generateExpr(cond, env)
        val trueResult = generateExpr(`true`, condResult.env)
        val falseResult = generateExpr(`false`, trueResult.env)
        Result(
                Operation.If(condResult.operation, trueResult.operation, falseResult.operation),
                condResult.declarations + trueResult.declarations + falseResult.declarations,
                falseResult.env
        )
    }

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
                Operation.Lambda(expr.path.toList, mangledName.toList, List.valueOf(closureOperations)),
                Declaration.Lambda(mangledName.toList, expr.path.toList, List.valueOf(closures.map(String::toList)), result.operation).cons(result.declarations),
                result.env.copy(locals = newEnv.locals, def = newEnv.def, index = newEnv.index)
        )
    }

    fun generateDefExpr(name: String, expr: Expr, env: Env): Result = if (env[name] != null) {
        if (name in internals.keys) generateIdentifierExpr(name, env) else throw Exception("$name has already been defined")
    } else {
        val newEnv = env.copy(globals = env.globals + (name to Variable.Global(name.toList, expr.path.toList)))
        val result = generateExpr(expr, newEnv.copy(def = name))
        Result(
                Operation.Def(name.toList, expr.path.toList, result.operation),
                Declaration.Def(name.toList, expr.path.toList, result.operation).cons(result.declarations),
                result.env.copy(def = newEnv.def)
        )
    }

    fun generateDoExpr(expr: Expr, env: Env): Result = run {
        val result = generateExpr(expr, env)
        Result(
                Operation.Do(result.operation),
                result.declarations,
                result.env
        )
    }

    fun generateSymbolExpr(name: String, env: Env): Result = Result(Operation.Symbol(name.toList), nil(), env)

    tailrec fun generateApplyExpr(fn: Expr, args: kotlin.collections.List<Expr>, env: Env): Result = when (args.size) {
        0 -> generateApplyExpr(fn, listOf(Expr.List(emptyList(), fn.path, fn.start, fn.end)), env)
        1 -> {
            val fnResult = generateExpr(fn, env)
            val argResult = generateExpr(args.first(), fnResult.env)
            Result(
                    Operation.Apply(fnResult.operation, argResult.operation),
                    fnResult.declarations + argResult.declarations,
                    argResult.env
            )
        }
        else -> generateApplyExpr(Expr.List(listOf(fn, args.first()), fn.path, fn.start, fn.end), args.drop(1), env)
    }

    fun generateListExpr(expr: Expr.List, env: Env): Result = if (expr.exprs.isEmpty()) {
        generateNil(env)
    } else {
        val exprs = expr.exprs
        when ((exprs.first() as? Expr.Identifier)?.name) {
            "if" -> generateIfExpr(exprs[1], exprs[2], exprs[3], env)
            "lambda" -> generateLambdaExpr((exprs[1] as Expr.Identifier).name, exprs[2], env)
            "def" -> generateDefExpr((exprs[1] as Expr.Identifier).name, exprs[2], env)
            "do" -> generateDoExpr(exprs[1], env)
            "symbol" -> generateSymbolExpr((exprs[1] as Expr.Identifier).name, env)
            else -> generateApplyExpr(exprs.first(), exprs.drop(1), env)
        }
    }

    fun generateExpr(expr: Expr, env: Env): Result = try {
        when (expr) {
            is Expr.Identifier -> generateIdentifierExpr(expr.name, env)
            is Expr.List -> generateListExpr(expr, env)
        }.run { copy(operation = Operation.LineNumber(operation, Nat(expr.start.line))) }
    } catch (e: Exception) {
        e.stackTrace += StackTraceElement(expr.path, env.def, "${expr.path.substringAfterLast('.')}.m", expr.start.line.toInt())
        throw e
    }

    fun generate(env: Env): Result = if (env.exprs.none()) {
        Result(Operation.Nil, nil(), env)
    } else {
        val car = generateExpr(env.exprs.car, env.copy(exprs = env.exprs.cdr))
        val cdr = generate(car.env)
        Result(
                Operation.Combine(car.operation, cdr.operation),
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

    fun generateProgram(out: File, @Suppress("UNUSED_PARAMETER") operation: Operation, declarations: Sequence<Declaration>) {
        declarations
                .groupBy { it.path.toString }
                .forEach { path, decls -> write(Declaration.clazz(path, decls.asSequence()), out, path) }
    }

    fun generate(`in`: File, out: File) {
        val exprs = Parser.parse(`in`, "", true).asCons()
        val env = Env(exprs, emptyMap(), internals, "", 0U)
        val result = generate(env)
        generateProgram(out, result.operation, result.declarations)
    }

    @Suppress("unused")
    object Definitions {
        @MField("mangle-lambda-name")
        @JvmField
        val mangleLambdaName: Value = Function { name, index ->
            Generator.mangleLambdaName(List.from(name).toString, Nat.from(index).value).toList
        }

        @MField("internal-variables")
        @JvmField
        val internalVariables: Value = List.valueOf(internals.entries.map { Pair.Impl(it.key.toList, it.value) }.asSequence())

        @MField("generate-program")
        @JvmField
        val generateProgram: Value = Function { out, operation, declarations ->
            Process {
                Generator.generateProgram(out as File, operation as Operation, List.from(declarations).asSequence().map { it as Declaration })
                List.nil
            }
        }

        @MField("debug")
        @JvmField
        val debug: Value = Function { x ->
            println(x)
            x
        }
    }
}