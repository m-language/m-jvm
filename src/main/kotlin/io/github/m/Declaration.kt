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

    data class Def(val name: List, val path: List, val value: Operation) : Data.Abstract("def-declaration", "name" to name, "path" to path, "value" to value), Declaration {
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
    }

    data class Lambda(val name: List, val closures: List, val value: Operation) : Data.Abstract("lambda-declaration", "name" to name, "closures" to closures, "value" to value), Declaration {
        override fun ClassWriter.generate() {
            val args = (0..closures.count()).joinToString(", ", "(", ")") { "io.github.m.Value" }
            val type = Method.getMethod("io.github.m.Value ${name.toString} $args")
            GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC, type, null, null, this).apply {
                value.apply { generate() }
                returnValue()
                endMethod()
            }
        }
    }

    data class Combine(val first: Declaration, val second: Declaration) : Data.Abstract("combine-declaration", "first" to first, "second" to second), Declaration {
        override fun ClassWriter.generate() {
            first.apply { generate() }
            second.apply { generate() }
        }
    }

    object None : Data.Abstract("no-declaration"), Declaration {
        override fun ClassWriter.generate() {

        }
    }

    companion object {
        fun mainClass(
                name: String,
                operation: Operation,
                declaration: Declaration
        ): ByteArray = ClassWriter(ClassWriter.COMPUTE_FRAMES).run {
            val pathName = name.replace('.', '/')
            val type = Type.getType("L$pathName;")
            visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, pathName, null, "Ljava/lang/Object;", null)

            declaration.apply { generate() }

            val run = visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "_run_", "Z", null, null)
            run.visitEnd()

            GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, Method.getMethod("void run ()"), null, null, this).apply {
                val endLabel = newLabel()

                getStatic(type, "_run_", Type.BOOLEAN_TYPE)
                ifZCmp(GeneratorAdapter.NE, endLabel)

                push(true)
                putStatic(type, "_run_", Type.BOOLEAN_TYPE)

                operation.apply { generate() }
                pop()

                mark(endLabel)

                returnValue()
                endMethod()
            }

            GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, Method.getMethod("void main (java.lang.String[])"), null, null, this).apply {
                visitLineNumber(1, newLabel())
                loadArg(0)
                push(type)
                invokeStatic(Type.getType("Lio/github/m/Internals;"), Method.getMethod("void run (java.lang.String[], java.lang.Class)"))
                returnValue()
                endMethod()
            }

            visitSource("$name.m", null)

            visitEnd()
            toByteArray()
        }
    }
}