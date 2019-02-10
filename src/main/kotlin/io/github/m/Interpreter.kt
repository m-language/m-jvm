package io.github.m

/**
 * Bindings for the M interpreter.
 */
object Interpreter {
    class ClassLoader : java.lang.ClassLoader() {
        fun define(name: String, bytes: ByteArray) = defineClass(name, bytes, 0, bytes.size)!!.also { resolveClass(it) }
    }

    @ExperimentalUnsignedTypes
    class Heap(val definitions: Value, private val classLoader: ClassLoader) : Value {
        fun load(declaration: Declaration) = load(sequenceOf(declaration))

        fun load(declarations: Sequence<Declaration>): Heap = run {
            val bytes = Generator.generateProgram(Operation.Nil, declarations)
            val newDefinitions = bytes
                    .map { (path, bytes) -> load(path, bytes) }
                    .map { clazz -> fields(clazz) }
                    .plus(emptyMap())
                    .reduce { acc, map -> acc + map }
            val fn = Value @Suppress("RedundantLambdaArrow") { x ->
                newDefinitions[x.toString]?.let { Either.Right(Value { _ -> it }) } ?: definitions(x)
            }
            Heap(fn, classLoader)
        }

        private fun load(path: String, bytes: ByteArray) = classLoader.define(path, bytes)

        private fun fields(clazz: Class<*>) = clazz.fields.asSequence()
                .filter { field -> field.isAnnotationPresent(MField::class.java) }
                .map { field -> field.getAnnotation(MField::class.java).name to field.get(null) as Value }
                .toMap()

        override fun invoke(arg: Value): Value = definitions(arg)

        companion object {
            fun from(value: Value) = value as? Heap ?: Heap(value, ClassLoader())
        }
    }

    /**
     * M interpreter definitions.
     */
    @Suppress("unused")
    @ExperimentalUnsignedTypes
    object Definitions {
        @MField("interpret-declaration")
        @JvmField
        val interpretDeclaration: Value = Value { declaration, heap ->
            Heap.from(heap).load(declaration as Declaration)
        }

        @MField("interpret-declarations")
        @JvmField
        val interpretDeclarations: Value = Value { declarations, heap ->
            Heap.from(heap).load((declarations as List).asSequence().map { it as Declaration })
        }
    }
}