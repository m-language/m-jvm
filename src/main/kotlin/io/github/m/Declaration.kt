package io.github.m

import io.github.m.List.Nil
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import jdk.internal.org.objectweb.asm.Type
import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter
import jdk.internal.org.objectweb.asm.commons.Method

/**
 * A declaration for a class.
 */
interface Declaration : Value {
    fun ClassWriter.generate()

    val path: List
    val init: Operation

    data class Def(val name: List, override val path: List, val value: Operation) : Data.Abstract("def-declaration", "name" to name, "path" to path, "value" to value), Declaration {
        override fun ClassWriter.generate() {
            val field = visitField(
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                    name.toString.normalize(),
                    "Lio/github/m/Value;",
                    null,
                    null
            )

            val av = field.visitAnnotation("Lio/github/m/MField;", true)
            av.visit("name", name.toString)
            av.visitEnd()

            field.visitEnd()
        }

        override val init get() = object : Operation {
            override fun invoke(arg: Value) = arg
            override fun GeneratorAdapter.generate() {
                internals[name.toString]?.invoke(this) ?: value.apply { generate() }
                putStatic(Type.getType("L${path.toString};"), name.toString.normalize(), Type.getType("Lio/github/m/Value;"))
            }
        }

        companion object {
            val internals: Map<String, (GeneratorAdapter) -> Unit> = listOf<java.lang.Class<*>>(
                    Bool.Definitions::class.java,
                    Char.Definitions::class.java,
                    Data.Definitions::class.java,
                    Declaration.Definitions::class.java,
                    Either.Definitions::class.java,
                    Errors::class.java,
                    File.Definitions::class.java,
                    Generator.Definitions::class.java,
                    Interpreter.Definitions::class.java,
                    List.Definitions::class.java,
                    Nat.Definitions::class.java,
                    Operation.Definitions::class.java,
                    Pair.Definitions::class.java,
                    Process.Definitions::class.java,
                    Stdio.Definitions::class.java,
                    io.github.m.Symbol.Definitions::class.java
            ).flatMap {
                val type = Type.getType(it.name.replace('.', '/'))
                it.fields.asSequence()
                        .filter { field -> field.isAnnotationPresent(MField::class.java) }
                        .map { field -> field.getAnnotation(MField::class.java).name to { ge: GeneratorAdapter -> ge.getStatic(type, field.name, Type.getType("Lio/github/m/Value;")) } }
                        .toList()
            }.toMap()
        }
    }

    data class Fn(val name: List, override val path: List, val closures: List, val value: Operation) : Data.Abstract("fn-declaration", "name" to name, "path" to path, "closures" to closures, "value" to value), Declaration {
        override fun ClassWriter.generate() {
            val args = (0..closures.count()).joinToString(", ", "(", ")") { "io.github.m.Value" }
            val type = Method.getMethod("io.github.m.Value ${name.toString.normalize()} $args")
            GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC, type, null, null, this).apply {
                value.apply { generate() }
                returnValue()
                endMethod()
            }
        }

        override val init get() = object : Operation {
            override fun invoke(arg: Value) = arg
            override fun GeneratorAdapter.generate() {

            }
        }
    }

    companion object {
        fun clazz(
                path: String,
                declarations: Sequence<Declaration>
        ): ByteArray = object : ClassWriter(ClassWriter.COMPUTE_FRAMES) { }.run {
            val type = Type.getType("L$path;")
            visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, path, null, "java/lang/Object", null)

            declarations.forEach {
                it.apply { generate() }
            }

            GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, Method.getMethod("void <clinit> ()"), null, null, this).apply {
                declarations.forEach {
                    it.init.apply { generate() }
                }

                returnValue()
                endMethod()
            }

            if (declarations.any { it is Declaration.Def && it.name == Nil }) {
                GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, Method.getMethod("void main (java.lang.String[])"), null, null, this).apply {
                    loadArg(0)
                    push(type)
                    invokeStatic(Type.getType("Lio/github/m/Internals;"), Method.getMethod("void run (java.lang.String[], java.lang.Class)"))
                    returnValue()
                    endMethod()
                }
            }

            visitSource("${path.substringAfterLast('/')}.m", null)

            visitEnd()
            toByteArray()
        }
    }

    /**
     * M declaration definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("def-declaration")
        @JvmField
        val def: Value = Value { name, path, value ->
            Declaration.Def(List.from(name), List.from(path), value as Operation)
        }

        @MField("fn-declaration")
        @JvmField
        val fn: Value = Value { name, path, closures, value ->
            Declaration.Fn(List.from(name), List.from(path), List.from(closures), value as Operation)
        }
    }
}