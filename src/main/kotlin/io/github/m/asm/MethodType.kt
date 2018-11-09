package io.github.m.asm

/**
 * Class representing the type of a def.
 *
 * @param name      The name of the def.
 * @param signature The signature of the def.
 */
data class MethodType(
        val name: String,
        val signature: MethodSignature
) {
    constructor(
            name: String,
            generics: List<Generic>,
            returnType: Type,
            paramTypes: List<Type>,
            exceptions: Set<Type>
    ) : this(name, MethodSignature(generics, returnType, paramTypes, exceptions))

    internal val asm
        get() = jdk.internal.org.objectweb.asm.commons.Method.getMethod(
                "${signature.returnType.name} $name ${signature.paramTypes.joinToString(", ", "(", ")") { it.name }}",
                true)

    companion object {
        /**
         * Creates the def type for a constructor.
         */
        fun constructor(
                paramTypes: List<Type>,
                exceptions: Set<Type>
        ) = MethodType(
                "<init>",
                emptyList(),
                Type.void,
                paramTypes,
                exceptions
        )
    }
}