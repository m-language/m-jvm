package io.github.m

/**
 * Bindings for the M interpreter.
 */
object Interpreter {
    class ClassLoader : java.lang.ClassLoader() {
        fun define(path: String, bytes: ByteArray) = try {
            defineClass(path, bytes, 0, bytes.size)!!.also { resolveClass(it) }
        } catch (e: Error) {
            throw Exception("Error loading class $path", e)
        }
    }

    val mPath = "io/github/m/heap".toList

    fun Declaration.rename(): Declaration = when (this) {
        is Declaration.Def -> copy(path = mPath, _value = _value.rename())
        is Declaration.Fn -> copy(path = mPath, _value = _value.rename())
        else -> TODO(this::class.java.name)
    }

    fun Operation.rename(): Operation = when (this) {
        is Operation.LocalVariable -> this
        is Operation.GlobalVariable -> copy(path = mPath)
        is Operation.If -> copy(cond = cond.rename(), `true` = `true`.rename(), `false` = `false`.rename())
        is Operation.Def -> copy(path = mPath, _value = _value.rename())
        is Operation.Fn -> copy(path = mPath, _value = _value.rename())
        is Operation.Symbol -> this
        is Operation.Apply -> copy(fn = fn.rename(), arg = arg.rename())
        is Operation.LineNumber -> copy(operation = operation.rename())
        is Operation.Nil -> this
        else -> TODO(this::class.java.name)
    }

    class Heap(val declarations: Map<String, Declaration>,
               val cache: MutableMap<String, Value>,
               val fallback: Value) : Value {
        val clazz by lazy {
            val bytes = Backend.clazz("io/github/m/heap", declarations.values.asSequence())
            ClassLoader().define("io.github.m.heap", bytes)
        }

        val fieldNames by lazy {
            clazz.fields
                    .map { it.getAnnotation(MField::class.java).name to it }
                    .toMap()
        }

        fun interpret(declarations: Sequence<Declaration>): Heap {
            val newDeclarations = declarations
                    .map { it.rename() }
                    .associateBy { it.name.toString }
            newDeclarations.forEach { cache.remove(it.key) }
            return Heap(this.declarations + newDeclarations, cache, fallback)
        }

        override fun invoke(arg: Value): Value {
            val name = arg.toString
            val value = cache[name] ?: fieldNames[name]?.get(null) as? Value
            return if (value != null) {
                cache.putIfAbsent(name, value)
                Either.Right(Value @Suppress("RedundantLambdaArrow") { _ -> value })
            } else {
                fallback(arg)
            }
        }

        companion object {
            fun from(value: Value) = value as? Heap ?: Heap(emptyMap(), mutableMapOf(), value)
        }
    }

    /**
     * M interpreter definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField(name = "interpret-declarations")
        @JvmField
        val interpretDeclarations: Value = Value.Impl2 { declarations, heap ->
            Heap.from(heap).interpret((declarations as List).asSequence().map { it as Declaration })
        }

        @MField(name = "interpret-declaration")
        @JvmField
        val interpretDeclaration: Value = Value.Impl2 { declaration, heap ->
            Heap.from(heap).interpret(sequenceOf(declaration as Declaration))
        }

        @MField(name = "interpret-def-declaration")
        @JvmField
        val interpretDefDeclaration: Value = interpretDeclaration

        @MField(name = "interpret-fn-declaration")
        @JvmField
        val interpretFnDeclaration: Value = interpretDeclaration
    }
}