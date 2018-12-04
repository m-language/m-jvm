package io.github.m

import java.nio.file.StandardOpenOption

@Suppress("MemberVisibilityCanBePrivate")
@ExperimentalUnsignedTypes
object Generator {
    val internals: Map<String, Variable> = listOf<java.lang.Class<*>>(
            Bool.Definitions::class.java,
            Char.Definitions::class.java,
            Declaration.Definitions::class.java,
            Errors::class.java,
            File.Definitions::class.java,
            Function.Definitions::class.java,
            Generator::class.java,
            Int.Definitions::class.java,
            Internals::class.java,
            List.Definitions::class.java,
            Nat.Definitions::class.java,
            Operation.Definitions::class.java,
            Pair.Definitions::class.java,
            Process.Definitions::class.java,
            Real.Definitions::class.java,
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
        is Expr.Identifier -> when (env.vars[expr.name]) {
            null -> emptySet()
            is Variable.Global -> emptySet()
            is Variable.Local -> setOf(expr.name)
        }
        is Expr.List -> expr.exprs.flatMap { closures(it, env) }.toSet()
    }

    fun generateIdentifierExpr(name: String, env: Env) =
            GenerateResult(
                    when (val variable = env.vars[name]) {
                        is Variable.Local -> Operation.LocalVariable(variable.name, variable.index)
                        is Variable.Global -> Operation.GlobalVariable(variable.name, variable.path)
                        null -> Operation.ReflectiveVariable(name.toList, env.path.toList)
//                            throw Exception("Could not find ${name.toString}")
                    },
                    Declaration.None,
                    env
            )

    fun generateNil(env: Env) = GenerateResult(Operation.Nil, Declaration.None, env)

    fun generateIfExpr(cond: Expr, `true`: Expr, `false`: Expr, env: Env): GenerateResult = run {
        val condResult = generateExpr(cond, env)
        val trueResult = generateExpr(`true`, condResult.env)
        val falseResult = generateExpr(`false`, trueResult.env)
        GenerateResult(
                Operation.If(condResult.operation, trueResult.operation, falseResult.operation),
                Declaration.Combine(condResult.declaration, Declaration.Combine(trueResult.declaration, falseResult.declaration)),
                falseResult.env
        )
    }

    fun generateLambdaExpr(name: String, expr: Expr, env: Env): GenerateResult = run {
        val methodName = mangleLambdaName(env.def, env.index)
        val env2 = env.copy(index = env.index + 1U)
        val closures = closures(expr, env).asSequence()
        val closureOperations = closures.map { generateIdentifierExpr(it, env2).operation }
        val (_, locals) = closures.plus(element = name).fold(0 to env.vars) { (index, map), name ->
            index + 1 to map + (name to Variable.Local(name.toList, Nat(index.toUInt())))
        }
        val exprResult = generateExpr(expr, env2.copy(vars = locals, def = methodName))
        GenerateResult(
                Operation.Lambda(env2.path.toList, methodName.toList, List.valueOf(closureOperations)),
                Declaration.Combine(exprResult.declaration, Declaration.Lambda(methodName.toList, List.valueOf(closures.map(String::toList)), exprResult.operation)),
                env2
        )
    }

    fun generateDefExpr(name: String, expr: Expr, env: Env): GenerateResult = run {
        val env2 = env.copy(vars = env.vars + (name to Variable.Global(name.toList, env.path.toList)))
        val localEnv = env2.copy(def = name)
        val exprResult = generateExpr(expr, localEnv)
        if (env.vars[name] == null) {
            GenerateResult(
                    Operation.Def(name.toList, exprResult.operation, localEnv.path.toList),
                    Declaration.Combine(exprResult.declaration, Declaration.Def(name.toList, env.path.toList, exprResult.operation)),
                    env2
            )
        } else {
            GenerateResult(
                    generateIdentifierExpr(name, env).operation,
                    Declaration.None,
                    env
            )
        }
    }

    fun generateSymbolExpr(name: String, env: Env): GenerateResult =
            GenerateResult(Operation.Symbol(name.toList), Declaration.None, env)

    tailrec fun generateApplyExpr(fn: Expr, args: kotlin.collections.List<Expr>, env: Env): GenerateResult = when (args.size) {
        0 -> generateApplyExpr(fn, listOf(Expr.List(emptyList(), fn.start, fn.end)), env)
        1 -> {
            val fnResult = generateExpr(fn, env)
            val argResult = generateExpr(args.first(), fnResult.env)
            GenerateResult(
                    Operation.Apply(fnResult.operation, argResult.operation),
                    Declaration.Combine(fnResult.declaration, argResult.declaration),
                    argResult.env
            )
        }
        else -> generateApplyExpr(Expr.List(listOf(fn, args.first()), fn.start, fn.end), args.drop(1), env)
    }

    fun generateListExpr(expr: Expr.List, env: Env): GenerateResult =
            if (expr.exprs.isEmpty()) {
                generateNil(env)
            } else {
                val exprs = expr.exprs
                when ((exprs.first() as? Expr.Identifier)?.name) {
                    "if" -> generateIfExpr(exprs[1], exprs[2], exprs[3], env)
                    "lambda" -> generateLambdaExpr((exprs[1] as Expr.Identifier).name, exprs[2], env)
                    "def" -> generateDefExpr((exprs[1] as Expr.Identifier).name, exprs[2], env)
                    "symbol" -> generateSymbolExpr((exprs[1] as Expr.Identifier).name, env)
                    else -> generateApplyExpr(exprs.first(), exprs.drop(1), env)
                }
            }

    fun generateExpr(expr: Expr, env: Env): GenerateResult = try {
        when (expr) {
            is Expr.Identifier -> generateIdentifierExpr(expr.name, env)
            is Expr.List -> generateListExpr(expr, env)
        }.run { copy(operation = Operation.LineNumber(operation, Nat(expr.start.line))) }
    } catch (e: Exception) {
        throw Exception(e.message + "\n    at ${expr.start}")
    }

    fun generateExprs(exprs: Seq<Expr>, env: Env): GenerateResult = when (exprs) {
        Seq.Nil -> GenerateResult(Operation.Nil, Declaration.None, env)
        is Seq.Cons -> {
            val generateResultCar = generateExpr(exprs.car, env)
            val generateResultCdr = generateExprs(exprs.cdr, generateResultCar.env)
            GenerateResult(
                    Operation.Combine(generateResultCar.operation, generateResultCdr.operation),
                    Declaration.Combine(generateResultCar.declaration, generateResultCdr.declaration),
                    generateResultCdr.env
            )
        }
    }

    fun generateProgram(name: String, out: File, operation: Operation, declaration: Declaration) {
        val clazz = Declaration.mainClass(name, operation, declaration)
        val file = java.io.File(out.value, "${name.replace('.', '/')}.class")
        val path = file.toPath()
        file.parentFile.mkdirs()
        if (file.exists()) java.nio.file.Files.delete(path)
        java.nio.file.Files.write(path, clazz, StandardOpenOption.CREATE)
    }

    fun generate(name: String, out: File, exprs: Seq<Expr>) = run {
        val env = Env(internals, name, "", 0U)
        val result = generateExprs(exprs, env)
        generateProgram(name, out, result.operation, result.declaration)
    }

    @Suppress("unused")
    @MField("mangle-lambda-name")
    @JvmField
    val mangleLambdaName: Value = Function { name, index ->
        Generator.mangleLambdaName((name as List).toString, (index as Nat).value).toList
    }

    @Suppress("unused")
    @MField("internal-variables")
    @JvmField
    val internalVariables: Value = List.valueOf(internals.entries.map { Pair.Impl(it.key.toList, it.value) }.asSequence())

    @Suppress("unused")
    @MField("generate-program")
    @JvmField
    val generateProgram: Value = Function { name, out, operation, declaration ->
        Process {
            Generator.generateProgram(name.toString, out as File, operation as Operation, declaration as Declaration)
            List.Nil
        }
    }

    @Suppress("unused")
    @MField("debug")
    @JvmField
    val debug: Value = Function { x ->
        println(x)
        x
    }
}