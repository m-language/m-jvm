package io.github.m

import java.nio.file.StandardOpenOption

@Suppress("MemberVisibilityCanBePrivate")
data class Compiler(val path: String) {
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

    fun closures(expr: Tree, env: Env): Set<String> = when (expr) {
        is Tree.Val -> when (env[Symbol.toString(expr.name)]) {
            null -> emptySet()
            is Variable.Global -> emptySet()
            is Variable.Local -> setOf(Symbol.toString(expr.name))
        }
        is Tree.Def -> closures(expr._value, env)
        is Tree.Fn -> closures(expr._value, env)
        is Tree.Ap -> closures(expr.fn, env) + closures(expr.arg, env)
        else -> emptySet()
    }

    fun generateVal(name: String, env: Env): Result =
            when (val variable = env[name]) {
                is Variable.Local -> Result(Operation.LocalVariable(variable.name, variable.index), nil(), env)
                is Variable.Global -> Result(Operation.GlobalVariable(variable.name, variable.path), nil(), env)
                null -> throw Exception("Could not find $name")
            }

    fun generateFn(name: String, expr: Tree, env: Env): Result = run {
        val mangledName = mangleLambdaName(env.def, env.index)
        val newEnv = env.copy(index = env.index + 1)
        val closures = closures(expr, env).asSequence()
        val closureOperations = closures.map { generateVal(it, newEnv).operation }
        val locals = newEnv.locals + closures
                .plus(element = name)
                .withIndex()
                .map { (index, name) -> name to Variable.Local(Symbol.toList(name), Nat.valueOf(index)) }
                .toMap()
        val result = generate(expr, newEnv.copy(locals = locals, def = mangledName))
        Result(
                Operation.Fn(Symbol.toList(path), Symbol.toList(mangledName), Symbol.toList(name), result.operation, closureOperations.list()),
                Declaration.Fn(Symbol.toList(mangledName), Symbol.toList(path), closures.map { Symbol.toList(it) }.list(), result.operation).cons(result.declarations),
                result.env.copy(locals = newEnv.locals, def = newEnv.def, index = newEnv.index)
        )
    }

    fun generateDef(name: String, expr: Tree, env: Env): Result = if (env[name] != null) {
        throw Exception("$name has already been defined")
    } else {
        val newEnv = env.copy(globals = env.globals + (name to Variable.Global(Symbol.toList(name), Symbol.toList(path))))
        val result = generate(expr, newEnv.copy(def = name))
        Result(
                Operation.Def(Symbol.toList(name), Symbol.toList(path), result.operation),
                Declaration.Def(Symbol.toList(name), Symbol.toList(path), result.operation).cons(result.declarations),
                result.env.copy(def = newEnv.def)
        )
    }

    fun generateAp(fn: Tree, arg: Tree, env: Env): Result = run {
        val fnResult = generate(fn, env)
        val argResult = generate(arg, fnResult.env)
        Result(
                Operation.Apply(fnResult.operation, argResult.operation),
                fnResult.declarations + argResult.declarations,
                argResult.env
        )
    }

    fun generateSymbol(name: String, env: Env): Result = Result(Operation.Symbol(Symbol.toList(name)), nil(), env)

    fun generate(tree: Tree, env: Env): Result = try {
        when (tree) {
            is Tree.Val -> generateVal(Symbol.from(tree.name).value, env)
            is Tree.Def -> generateDef(Symbol.from(tree.name).value, tree._value, env)
            is Tree.Fn -> generateFn(Symbol.from(tree.arg).value, tree._value, env)
            is Tree.Ap -> generateAp(tree.fn, tree.arg, env)
            is Tree.Symbol -> generateSymbol(Symbol.from(tree.name).value, env)
            else -> TODO(tree.toString())
        }
    } catch (e: Failure) {
        throw e
    } catch (e: Exception) {
        throw Failure("${e.message} in ${env.def}")
    }

    fun generate(trees: Sequence<Tree>, env: Env): Result = if (trees.none()) {
        Result(Operation.Nil, nil(), env)
    } else {
        val car = generate(trees.car, env)
        val cdr = generate(trees.cdr, car.env)
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

        fun tree(expr: Expr): Tree = when (expr) {
            is Expr.Symbol -> Tree.Val(Symbol.toList(expr.name))
            is Expr.List -> {
                val exprs = expr.exprs
                when ((exprs.first() as? Expr.Symbol)?.name) {
                    "def" -> Tree.Def(Symbol.toList((exprs[1] as Expr.Symbol).name), tree(exprs[2]))
                    "fn" -> Tree.Fn(Symbol.toList((exprs[1] as Expr.Symbol).name), tree(exprs[2]))
                    "symbol" -> Tree.Symbol(Symbol.toList((exprs[1] as Expr.Symbol).name))
                    else -> Tree.Ap(tree(exprs[0]), tree(exprs[1]))
                }
            }
        }

        fun compile(`in`: File, out: File, trees: Sequence<Tree>) {
            val env = Env(emptyMap(), emptyMap(), "", 0)
            val result = Compiler(`in`.nameWithoutExtension).generate(trees, env)
            writeProgram(out, result.operation, result.declarations)
        }

        fun compile(`in`: File, out: File) {
            val env = Env(emptyMap(), emptyMap(), "", 0)
            val exprs = Parser.parse(`in`).asCons()
            val trees = exprs.map { tree(it) }
            val result = Compiler(`in`.nameWithoutExtension).generate(trees, env)
            writeProgram(out, result.operation, result.declarations)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 2) {
                System.err.println("Usage: m <input> <output>")
            } else {
                val `in` = File(args[0])
                val out = File(args[1])
                try {
                    compile(`in`, out)
                } catch (e: Failure) {
                    System.err.println(e.message)
                }
            }
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

        @MField(name = "jvm-backend'")
        @JvmField
        val jvmBackendP: Value = Value.Impl3("jvm-backend'") { `in`, out, trees ->
            Process {
                compile(`in` as File, out as File, List.from(trees).asSequence().map { it as Tree })
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