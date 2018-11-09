package io.github.m.asm

/**
 * Class representing the signature for a def.
 *
 * @param generics   The generic parameters of the def.
 * @param returnType The return type of the def.
 * @param paramTypes The types of the parameters for the def.
 * @param exceptions The set of declared exception types for the def.
 */
data class MethodSignature(
        val generics: List<Generic>,
        val returnType: Type,
        val paramTypes: List<Type>,
        val exceptions: Set<Type>
) {
    /**
     * Returns the string representation of this def on the jvm.
     */
    fun internalString() = if (generics.isEmpty())
        ""
    else
        generics.joinToString("", "<", ">") {
            "${it.name}:${it.constraints.joinToString("", "", "") { type: Type -> ":${type.signature}" }}"
        } + paramTypes.joinToString("", "(", ")") { it.signature } +
                returnType.signature
}