package io.github.m

import io.github.m.asm.*

@Suppress("MemberVisibilityCanBePrivate")
object Generator {
    val stringCompare = Compare.list(Compare.from(Comparator.comparing<MAny, Char> { it.asChar.value }))

    val internals: List<MPair> = listOf<java.lang.Class<*>>(
            MAny.Definitions::class.java,
            MBool.Definitions::class.java,
            MList.Definitions::class.java,
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
            Generator.Definitions::class.java,
            GenerateResult.Definitions::class.java,
            Expr.Definitions::class.java,
            Env.Definitions::class.java,
            MPair.Definitions::class.java,
            TreeMap.Definitions::class.java,
            Variable.Definitions::class.java
    ).flatMap {
        it
                .fields
                .asSequence()
                .filter { field -> field.isAnnotationPresent(MField::class.java) }
                .map { field ->
                    val name = field.getAnnotation(MField::class.java).name.mString
                    val variable = Variable.Global(
                            field.name.mString,
                            MList.valueOf(QualifiedName.fromClass(it).list.map(String::mString))
                    )
                    MPair(name, variable)
                }
                .toList()
    }

    fun closures(expr: Expr, env: Env): Set<MString> = when (expr) {
        is Expr.Identifier -> {
            val maybe = env.vars.getValue(expr.name)
            when (maybe) {
                Maybe.None -> emptySet()
                is Maybe.Some -> when (maybe.value.cast<Variable>()) {
                    is Variable.Global -> emptySet()
                    is Variable.Local -> setOf(expr.name)
                }
            }
        }
        is Expr.List -> expr.exprs.flatMap { closures(it.cast(), env) }.toSet()
    }

    fun generateIdentifierExpr(name: MString, env: Env) =
            GenerateResult(
                    run {
                        val maybe = env.vars.getValue(name)
                        when (maybe) {
                            Maybe.None -> reflectiveVariableOperation(name.string, env.file.asType)
                            is Maybe.Some -> {
                                val variable = maybe.value.cast<Variable>()
                                when (variable) {
                                    is Variable.Local -> localVariableOperation(variable.name.string, variable.index.value)
                                    is Variable.Global -> globalVariableOperation(variable.name.string, variable.file.asType)
                                }
                            }
                        }
                    },
                    Declaration.empty,
                    env
            )

    fun generateNil(env: Env) = GenerateResult(nilOperation, Declaration.empty, env)

    fun generateIfExpr(cond: Expr, `true`: Expr, `false`: Expr, env: Env): GenerateResult = run {
        val condResult = generateExpr(cond, env)
        val trueResult = generateExpr(`true`, condResult.env)
        val falseResult = generateExpr(`false`, trueResult.env)
        GenerateResult(
                ifOperation(condResult.operation, trueResult.operation, falseResult.operation),
                block(condResult.declaration, trueResult.declaration, falseResult.declaration),
                falseResult.env
        )
    }

    fun generateLambdaExpr(name: MString, expr: Expr, env: Env): GenerateResult = run {
        val methodName = Definitions.mangleLambdaName.asFunction(env.def, env.index).asString
        val env2 = env.copy(index = env.index.add(MInt(1)))
        val closures = closures(expr, env)
        val closureOperations = closures.map { generateIdentifierExpr(it, env2).operation }
        val (_, locals) = closures.plus(element = name).fold(0 to env.vars) { (index, map), name ->
            index + 1 to map.putValue(name, Variable.Local(name, MInt(index)))
        }
        val exprResult = generateExpr(expr, env2.copy(vars = locals))
        GenerateResult(
                lambdaOperation(env2.file.asType, methodName, closureOperations),
                block(exprResult.declaration, lambdaDeclaration(methodName, closures.map { it.string }, exprResult.operation)),
                env2
        )
    }

    fun generateDefExpr(name: MString, expr: Expr, env: Env): GenerateResult = run {
        val env2 = env.copy(vars = env.vars.putValue(name, Variable.Global(name, env.file)))
        val localEnv = env2.copy(def = name)
        val exprResult = generateExpr(expr, localEnv)
        when (env.vars.getValue(name)) {
            Maybe.None -> GenerateResult(
                    defOperation(name.asString, exprResult.operation, localEnv.file.asType),
                    block(exprResult.declaration, defDeclaration(name.asString, env.file.asType)),
                    env2
            )
            is Maybe.Some -> GenerateResult(
                    generateIdentifierExpr(name, env).operation,
                    Declaration.empty,
                    env
            )
        }
    }

    fun generateSymbolExpr(name: MString, env: Env): GenerateResult =
            GenerateResult(symbolOperation(name.asString), Declaration.empty, env)

    tailrec fun generateApplyExpr(fn: Expr, args: MList, env: Env): GenerateResult = when (args) {
        is MList.Nil -> generateApplyExpr(fn, MList.Cons(Expr.List(MList.Nil, fn.line), MList.Nil), env)
        is MList.Cons -> when (args.cdr) {
            is MList.Cons -> generateApplyExpr(Expr.List(MList.Cons(fn, MList.Cons(args.car, MList.Nil)), fn.line), args.cdr, env)
            is MList.Nil -> {
                val fnResult = generateExpr(fn, env)
                val argResult = generateExpr(args.car.cast(), fnResult.env)
                GenerateResult(
                        applyOperation(fnResult.operation, argResult.operation),
                        block(fnResult.declaration, argResult.declaration),
                        argResult.env
                )
            }
        }
    }

    fun generateListExpr(expr: Expr.List, env: Env): GenerateResult = run {
        val exprs = expr.exprs
        when (exprs) {
            is MList.Nil -> generateNil(env)
            is MList.Cons -> when ((exprs.car as? Expr.Identifier)?.name?.asString) {
                "if" -> generateIfExpr(exprs.cadr.cast(), exprs.caddr.cast(), exprs.cadddr.cast(), env)
                "lambda" -> generateLambdaExpr(exprs.cadr.cast<Expr.Identifier>().name, exprs.caddr.cast(), env)
                "def" -> generateDefExpr(exprs.cadr.cast<Expr.Identifier>().name, exprs.caddr.cast(), env)
                "symbol" -> generateSymbolExpr(exprs.cadr.cast<Expr.Identifier>().name, env)
                else -> generateApplyExpr(exprs.car.cast(), exprs.cdr.cast(), env)
            }
        }
    }

    fun generateExpr(expr: Expr, env: Env): GenerateResult = try {
        when (expr) {
            is Expr.Identifier -> generateIdentifierExpr(expr.name, env)
            is Expr.List -> generateListExpr(expr, env)
        }.run { copy(operation = block(lineNumber(expr.line.value), operation)) }
    } catch (e: Error) {
        throw MError.Internal(e.message + "\n    at line ${expr.line}", e)
    }

    fun generateExprs(exprs: MList, env: Env): GenerateResult =
            when (exprs) {
                MList.Nil -> GenerateResult(Operation.empty, Declaration.empty, env)
                is MList.Cons -> {
                    val generateResultCar = generateExpr(exprs.car.cast(), env)
                    val generateResultCdr = generateExprs(exprs.cdr, generateResultCar.env)
                    GenerateResult(
                            block(generateResultCar.operation, pop, generateResultCdr.operation),
                            block(generateResultCar.declaration, generateResultCdr.declaration),
                            generateResultCdr.env
                    )
                }
            }

    @Suppress("unused")
    object Definitions {
        @MField("local-variable-operation")
        @JvmField
        val localVariableOperation: MAny = MFunction { name, index ->
            localVariableOperation(name.asString, index.asInt.value)
        }

        @MField("global-variable-operation")
        @JvmField
        val globalVariableOperation: MAny = MFunction { name, file ->
            globalVariableOperation(name.asString, file.asType)
        }

        @MField("reflective-variable-operation")
        @JvmField
        val reflectiveVariableOperation: MAny = MFunction { name, file ->
            reflectiveVariableOperation(name.asString, file.asType)
        }

        @MField("if-operation")
        @JvmField
        val ifOperation: MAny = MFunction { cond, `true`, `false` ->
            ifOperation(cond.asOperation, `true`.asOperation, `false`.asOperation)
        }

        @MField("def-operation")
        @JvmField
        val defOperation: MAny = MFunction { name, operation, env ->
            defOperation(name.asString, operation.asOperation, env.cast<Env>().file.asType)
        }

        @MField("def-declaration")
        @JvmField
        val defDeclaration: MAny = MFunction { name, env ->
            defDeclaration(name.asString, env.cast<Env>().file.asType)
        }

        @MField("lambda-operation")
        @JvmField
        val lambdaOperation: MAny = MFunction { file, name, closures ->
            lambdaOperation(file.asType, name.asString, closures.asList.map { it as Operation })
        }

        @MField("lambda-declaration")
        @JvmField
        val lambdaDeclaration: MAny = MFunction { name, closures, operation ->
            lambdaDeclaration(name.asString, closures.asList.map(MAny::asString), operation.asOperation)
        }

        @MField("symbol-operation")
        @JvmField
        val symbolOperation: MAny = MFunction { name ->
            symbolOperation(name.asString)
        }

        @MField("apply-operation")
        @JvmField
        val applyOperation: MAny = MFunction { fn, arg ->
            applyOperation(fn.asOperation, arg.asOperation)
        }

        @MField("nil-operation")
        @JvmField
        val nilOperation: MAny = io.github.m.asm.nilOperation

        @MField("no-operation")
        @JvmField
        val noOperation: MAny = Operation.empty

        @MField("no-declaration")
        @JvmField
        val noDeclaration: MAny = Declaration.empty

        @MField("combine-operation")
        @JvmField
        val combineOperation: MAny = MFunction { operation1, operation2 ->
            block(operation1.asOperation, operation2.asOperation)
        }

        @MField("ignore-result-operation")
        @JvmField
        val ignoreResultOperation: MAny = MFunction { operation ->
            block(operation.asOperation, pop)
        }

        @MField("line-number-operation")
        @JvmField
        val lineNumberOperation: MAny = MFunction { operation, line ->
            block(lineNumber(line.asInt.value), operation.asOperation)
        }

        @MField("combine-declaration")
        @JvmField
        val combineDeclaration: MAny = MFunction { declaration1, declaration2 ->
            block(declaration1.asDeclaration, declaration2.asDeclaration)
        }

        @MField("mangle-lambda-name")
        @JvmField
        val mangleLambdaName: MAny = MFunction { name, index ->
            "${name.asString}_${index.asInt}".mString
        }

        @MField("internal-variables")
        @JvmField
        val internalVariables: MAny = MList.valueOf(internals)

        @MField("generate-file")
        @JvmField
        val generateFile: MAny = MFunction { name, out, operation, declaration ->
            val clazzName = QualifiedName(listOf(name.asString))
            val clazz = mainClass(Type.clazz(clazzName), operation.asOperation, declaration.asDeclaration)
            MProcess {
                clazz.generate(out.asFile.file)
                MList.Nil
            }
        }

        @MField("~generate")
        @JvmField
        val generate: MAny = MFunction { name, out, exprs ->
            val internals = internalVariables.asList
                    .fold(TreeMap.empty(stringCompare)) { map, value ->
                        val pair = value.cast<MPair>()
                        map.putValue(pair.first, pair.second)
                    }

            val env = Env(internals, MList.Cons(name, MList.Nil), MList.Nil, MInt(0))
            val result = generateExprs(exprs.asList, env)
            generateFile.asFunction(name, out, result.operation, result.declaration)
        }

        @MField("~debug")
        @JvmField
        val debug: MAny = MFunction { x ->
            println(x)
            x
        }
    }
}