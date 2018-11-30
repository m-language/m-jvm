package io.github.m

import jdk.internal.org.objectweb.asm.Handle
import jdk.internal.org.objectweb.asm.Opcodes
import jdk.internal.org.objectweb.asm.Type
import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter
import jdk.internal.org.objectweb.asm.commons.Method

/**
 * An operation for a method.
 */
@ExperimentalUnsignedTypes
interface Operation : Value {
    fun GeneratorAdapter.generate()

    data class LocalVariable(val name: List, val index: Nat) : Data.Abstract("local-variable-operation", "name" to name, "index" to index), Operation {
        override fun GeneratorAdapter.generate() {
            loadArg(index.value.toInt())
        }
    }

    data class GlobalVariable(val name: List, val path: List) : Data.Abstract("global-variable-operation", "name" to name, "path" to path), Operation {
        override fun GeneratorAdapter.generate() {
            getStatic(Type.getType("L${path.toString.replace('.', '/')};"), name.toString, Type.getType("Lio/github/m/Value;"))
        }
    }

    data class ReflectiveVariable(val name: List, val path: List) : Data.Abstract("reflective-variable-operation", "name" to name, "path" to path), Operation {
        override fun GeneratorAdapter.generate() {
            getStatic(Type.getType("L${path.toString.replace('.', '/')};"), name.toString, Type.getType("Lio/github/m/Value;"))
        }
    }

    data class If(val cond: Operation, val `true`: Operation, val `false`: Operation) : Data.Abstract("if-operation", "cond" to cond, "true" to `true`, "false" to `false`), Operation {
        override fun GeneratorAdapter.generate() {
            val endLabel = newLabel()
            val falseLabel = newLabel()

            cond.apply { generate() }

            invokeStatic(Type.getType("Lio/github/m/Internals;"), Method.getMethod("boolean toPrimitiveBool (io.github.m.Value)"))

            ifZCmp(GeneratorAdapter.EQ, falseLabel)

            `true`.apply { generate() }
            goTo(endLabel)

            mark(falseLabel)

            `false`.apply { generate() }

            mark(endLabel)
        }
    }

    data class Def(val name: List, val value: Operation, val path: List) : Data.Abstract("def-operation", "name" to name, "value" to value, "path" to path), Operation {
        override fun GeneratorAdapter.generate() {
            value.apply { generate() }
            putStatic(Type.getType("L${path.toString.replace('.', '/')};"), name.toString, Type.getType("Lio/github/m/Value;"))
            getStatic(Type.getType("L${path.toString.replace('.', '/')};"), name.toString, Type.getType("Lio/github/m/Value;"))
        }
    }

    data class Lambda(val path: List, val name: List, val closures: List) : Data.Abstract("lambda-operation", "path" to path, "name" to name, "closures" to closures), Operation {
        override fun GeneratorAdapter.generate() {
            closures.forEach { (it as Operation).apply { generate() } }
            val closureTypes = (0 until closures.count()).joinToString("", "", "") { "Lio/github/m/Value;" }
            visitInvokeDynamicInsn(
                    "invoke",
                    "($closureTypes)Lio/github/m/Function;",
                    Handle(
                            Opcodes.H_INVOKESTATIC,
                            "java/lang/invoke/LambdaMetafactory",
                            "metafactory",
                            "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"
                    ),
                    Type.getType("(Lio/github/m/Value;)Lio/github/m/Value;"),
                    Handle(
                            Opcodes.H_INVOKESTATIC,
                            path.toString.replace('.', '/'),
                            name.toString,
                            "(${closureTypes}Lio/github/m/Value;)Lio/github/m/Value;"
                    ),
                    Type.getType("(Lio/github/m/Value;)Lio/github/m/Value;")
            )
        }
    }

    data class Symbol(val name: List) : Data.Abstract("symbol-operation", "name" to name), Operation {
        override fun GeneratorAdapter.generate() {
            newInstance(Type.getType("Lio/github/m/Symbol;"))
            dup()
            push(name.toString)
            invokeConstructor(
                    Type.getType("Lio/github/m/Symbol;"),
                    Method.getMethod("void <init> (java.lang.String)")
            )
        }
    }

    data class Apply(val fn: Operation, val arg: Operation) : Data.Abstract("apply-operation", "fn" to fn, "arg" to arg), Operation {
        override fun GeneratorAdapter.generate() {
            fn.apply { generate() }
            arg.apply { generate() }
            invokeStatic(
                    Type.getType("Lio/github/m/Internals;"),
                    Method.getMethod("io.github.m.Value apply (io.github.m.Value, io.github.m.Value)")
            )
        }
    }

    data class Combine(val first: Operation, val second: Operation) : Data.Abstract("combine-operation", "first" to first, "second" to second), Operation {
        override fun GeneratorAdapter.generate() {
            first.apply { generate() }
            pop()
            second.apply { generate() }
        }
    }

    data class LineNumber(val operation: Operation, val line: Nat) : Data.Abstract("line-number-operation", "operation" to operation, "line" to line), Operation {
        override fun GeneratorAdapter.generate() {
            visitLineNumber(line.value.toInt(), mark())
            operation.apply { generate() }
        }
    }

    object Nil : Data.Abstract("nil-operation"), Operation {
        override fun GeneratorAdapter.generate() {
            getStatic(Type.getType("Lio/github/m/Internals;"), "nil", Type.getType("Lio/github/m/Value;"))
        }
    }
}