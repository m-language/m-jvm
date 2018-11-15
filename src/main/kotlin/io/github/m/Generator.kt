package io.github.m

import java.nio.file.Files
import java.nio.file.StandardOpenOption

@Suppress("MemberVisibilityCanBePrivate")
@UseExperimental(ExperimentalUnsignedTypes::class)
object Generator {
    val internals: kotlin.collections.List<Pair> = listOf<java.lang.Class<*>>(
            Value.Definitions::class.java,
            Bool.Definitions::class.java,
            List.Definitions::class.java,
            Int.Definitions::class.java,
            Nat.Definitions::class.java,
            Real.Definitions::class.java,
            Char.Definitions::class.java,
            Symbol.Definitions::class.java,
            Data.Definitions::class.java,
            Process.Definitions::class.java,
            File.Definitions::class.java,
            Undefined.Definitions::class.java,
            Runtime.Definitions::class.java,
            Generator.Definitions::class.java,
            Expr.Definitions::class.java,
            Pair.Definitions::class.java,
            Variable.Definitions::class.java,
            Operation.Definitions::class.java,
            Declaration.Definitions::class.java
    ).flatMap {
        it
                .fields
                .asSequence()
                .filter { field -> field.isAnnotationPresent(MField::class.java) }
                .map { field ->
                    val name = field.getAnnotation(MField::class.java).name.m
                    val variable = Variable.Global(field.name.m, it.name.m)
                    Pair(name, variable)
                }
                .toList()
    }

    fun closures(expr: Expr, env: Env): Set<List> = when (expr) {
        is Expr.Identifier -> when (env.vars[expr.name]) {
            null -> emptySet()
            is Variable.Global -> emptySet()
            is Variable.Local -> setOf(expr.name)
        }
        is Expr.List -> expr.exprs.flatMap { closures(it.cast(), env) }.toSet()
    }

    fun generateIdentifierExpr(name: List, env: Env) =
            GenerateResult(
                    when (val variable = env.vars[name]) {
                        is Variable.Local -> Operation.LocalVariable(variable.name, variable.index)
                        is Variable.Global -> Operation.GlobalVariable(variable.name, variable.path)
                        null -> Operation.ReflectiveVariable(name, env.path)
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

    fun generateLambdaExpr(name: List, expr: Expr, env: Env): GenerateResult = run {
        val methodName = Definitions.mangleLambdaName.asFunction(env.def, env.index).asList
        val env2 = env.copy(index = env.index.add(Nat(1.toUInt())))
        val closures = closures(expr, env).asSequence()
        val closureOperations = closures.map { generateIdentifierExpr(it, env2).operation }
        val (_, locals) = closures.plus(element = name).fold(0 to env.vars) { (index, map), name ->
            index + 1 to map + (name to Variable.Local(name, Nat(index.toUInt())))
        }
        val exprResult = generateExpr(expr, env2.copy(vars = locals, def = methodName))
        GenerateResult(
                Operation.Lambda(env2.path, methodName, List.valueOf(closureOperations)),
                Declaration.Combine(exprResult.declaration, Declaration.Lambda(methodName, List.valueOf(closures), exprResult.operation)),
                env2
        )
    }

    fun generateDefExpr(name: List, expr: Expr, env: Env): GenerateResult = run {
        val env2 = env.copy(vars = env.vars + (name to Variable.Global(name, env.path)))
        val localEnv = env2.copy(def = name)
        val exprResult = generateExpr(expr, localEnv)
        if (env.vars[name] == null) {
            GenerateResult(
                    Operation.Def(name, exprResult.operation, localEnv.path),
                    Declaration.Combine(exprResult.declaration, Declaration.Def(name, env.path)),
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

    fun generateSymbolExpr(name: List, env: Env): GenerateResult =
            GenerateResult(Operation.Symbol(name), Declaration.None, env)

    tailrec fun generateApplyExpr(fn: Expr, args: List, env: Env): GenerateResult = when (args) {
        is List.Nil -> generateApplyExpr(fn, List.Cons(Expr.List(List.Nil, fn.line), List.Nil), env)
        is List.Cons -> when (args.cdr) {
            is List.Cons -> generateApplyExpr(Expr.List(List.Cons(fn, List.Cons(args.car, List.Nil)), fn.line), args.cdr, env)
            is List.Nil -> {
                val fnResult = generateExpr(fn, env)
                val argResult = generateExpr(args.car.cast(), fnResult.env)
                GenerateResult(
                        Operation.Apply(fnResult.operation, argResult.operation),
                        Declaration.Combine(fnResult.declaration, argResult.declaration),
                        argResult.env
                )
            }
        }
    }

    fun generateListExpr(expr: Expr.List, env: Env): GenerateResult = when (val exprs = expr.exprs) {
        is List.Nil -> generateNil(env)
        is List.Cons -> when ((exprs.car as? Expr.Identifier)?.name?.asString) {
            "if" -> generateIfExpr(exprs.cadr.cast(), exprs.caddr.cast(), exprs.cadddr.cast(), env)
            "lambda" -> generateLambdaExpr(exprs.cadr.cast<Expr.Identifier>().name, exprs.caddr.cast(), env)
            "def" -> generateDefExpr(exprs.cadr.cast<Expr.Identifier>().name, exprs.caddr.cast(), env)
            "symbol" -> generateSymbolExpr(exprs.cadr.cast<Expr.Identifier>().name, env)
            else -> generateApplyExpr(exprs.car.cast(), exprs.cdr.cast(), env)
        }
    }

    fun generateExpr(expr: Expr, env: Env): GenerateResult = try {
        when (expr) {
            is Expr.Identifier -> generateIdentifierExpr(expr.name, env)
            is Expr.List -> generateListExpr(expr, env)
        }.run { copy(operation = Operation.LineNumber(operation, expr.line)) }
    } catch (e: java.lang.Error) {
        throw Error.Internal(e.message + "\n    at line ${expr.line}", e)
    }

    fun generateExprs(exprs: List, env: Env): GenerateResult = when (exprs) {
        List.Nil -> GenerateResult(Operation.Nil, Declaration.None, env)
        is List.Cons -> {
            val generateResultCar = generateExpr(exprs.car.cast(), env)
            val generateResultCdr = generateExprs(exprs.cdr, generateResultCar.env)
            GenerateResult(
                    Operation.Combine(generateResultCar.operation, generateResultCdr.operation),
                    Declaration.Combine(generateResultCar.declaration, generateResultCdr.declaration),
                    generateResultCdr.env
            )
        }
    }

    fun generate(name: List, out: File, exprs: List) = run {
        val internals = internals.map { it.first.asList to it.second.cast<Variable>() }.toMap()
        val env = Env(internals, name, List.Nil, Nat(0.toUInt()))
        val result = generateExprs(exprs.asList, env)
        Definitions.generateProgram.asFunction(name, out, result.operation, result.declaration)
    }

    @Suppress("unused")
    object Definitions {
        @MField("mangle-lambda-name")
        @JvmField
        val mangleLambdaName: Value = Function { name, index ->
            "${name.asString}_${index.asNat}".m
        }

        @MField("internal-variables")
        @JvmField
        val internalVariables: Value = List.valueOf(internals.asSequence())

        @MField("generate-program")
        @JvmField
        val generateProgram: Value = Function { name, out, operation, declaration ->
            val clazz = Declaration.mainClass(name.asString, operation.asOperation, declaration.asDeclaration)
            Process {
                val file = java.io.File(out.asFile.file, "${name.asString.replace('.', '/')}.class")
                val path = file.toPath()
                file.parentFile.mkdirs()
                if (file.exists()) Files.delete(path)
                Files.write(path, clazz, StandardOpenOption.CREATE)
                List.Nil
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