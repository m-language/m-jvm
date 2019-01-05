package io.github.m

import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import jdk.internal.org.objectweb.asm.Type
import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter
import jdk.internal.org.objectweb.asm.commons.Method

/**
 * A declaration for a class.
 */
@ExperimentalUnsignedTypes
interface Declaration : Value {
    fun ClassWriter.generate()

    val path: List
    val init: Operation

    data class Def(val name: List, override val path: List, val value: Operation) : Data.Abstract("def-declaration", "name" to name, "path" to path, "value" to value), Declaration {
        override fun ClassWriter.generate() {
            val field = visitField(
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                    name.toString,
                    "Lio/github/m/Value;",
                    null,
                    null
            )

            field.visitEnd()
        }

        override val init get() = object : Operation {
            override fun GeneratorAdapter.generate() {
                value.apply { generate() }
                putStatic(Type.getType("L${path.toString.replace('.', '/')};"), name.toString, Type.getType("Lio/github/m/Value;"))
            }
        }
    }

    data class Lambda(val name: List, override val path: List, val closures: List, val value: Operation) : Data.Abstract("lambda-declaration", "name" to name, "path" to path, "closures" to closures, "value" to value), Declaration {
        override fun ClassWriter.generate() {
            val args = (0..closures.count()).joinToString(", ", "(", ")") { "io.github.m.Value" }
            val type = Method.getMethod("io.github.m.Value ${name.toString} $args")
            GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC, type, null, null, this).apply {
                value.apply { generate() }
                returnValue()
                endMethod()
            }
        }

        override val init get() = object : Operation {
            override fun GeneratorAdapter.generate() {

            }
        }
    }

    companion object {
        fun clazz(
                name: String,
                declarations: Sequence<Declaration>
        ): ByteArray = object : ClassWriter(ClassWriter.COMPUTE_FRAMES) { }.run {
            val pathName = name.replace('.', '/')
            val type = Type.getType("L$pathName;")
            visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, pathName, null, "Ljava/lang/Object;", null)

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

            if (declarations.any { it is Declaration.Def && it.name == List.nil }) {
                GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, Method.getMethod("void main (java.lang.String[])"), null, null, this).apply {
                    loadArg(0)
                    push(type)
                    invokeStatic(Type.getType("Lio/github/m/Internals;"), Method.getMethod("void run (java.lang.String[], java.lang.Class)"))
                    returnValue()
                    endMethod()
                }
            }

            visitSource("${name.substringAfterLast('.')}.m", null)

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
        val def: Value = Function { name, path, value ->
            Declaration.Def(List.from(name), List.from(path), value as Operation)
        }

        @MField("lambda-declaration")
        @JvmField
        val lambda: Value = Function { name, path, closures, value ->
            Declaration.Lambda(List.from(name), List.from(path), List.from(closures), value as Operation)
        }
    }
}