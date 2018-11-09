package io.github.m.asm

import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter

/**
 * Class representing a method on the jvm.
 *
 * @param access     he access level of the method.
 * @param methodType The type of the method.
 * @param operation  The operation that makes up the method.
 */
data class Method(
        val access: Access,
        val methodType: MethodType,
        val operation: Operation
) : Declaration {
    constructor(
            access: Access,
            name: String,
            generics: List<Generic>,
            returnType: Type,
            paramTypes: List<Type>,
            exceptions: Set<Type>,
            operation: Operation
    ) : this(access, MethodType(name, generics, returnType, paramTypes, exceptions), operation)

    override fun generate(classWriter: ClassWriter) {
        val generatorAdapter = GeneratorAdapter(
                access.intValue,
                methodType.asm,
                methodType.signature.internalString().let { if (it.isEmpty()) null else it },
                methodType.signature.exceptions.map { it.asm }.toTypedArray(),
                classWriter
        )

        operation.generate(generatorAdapter)

        generatorAdapter.endMethod()
    }

    companion object {
        /**
         * Creates a def for a constructor.
         */
        fun constructor(
                access: Access,
                paramTypes: List<Type>,
                exceptions: Set<Type>,
                superType: Type,
                superSignature: MethodSignature,
                operation: Operation
        ) = Method(
                access,
                "<init>",
                emptyList(),
                Type.void,
                paramTypes,
                exceptions,
                block(operation, pushThis, invokeConstructor(superType, MethodType("<init>", superSignature)), `return`)
        )

        /**
         * Creates a def that is called upon the static initialization of an object.
         */
        fun staticInit(operation: Operation) = Method(
                Access().asPublic().asStatic(),
                "<clinit>",
                emptyList(),
                Type.void,
                emptyList(),
                emptySet(),
                block(operation, `return`)
        )
    }
}