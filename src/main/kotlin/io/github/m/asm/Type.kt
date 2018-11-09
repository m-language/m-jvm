@file:Suppress("unused")

package io.github.m.asm

import jdk.internal.org.objectweb.asm.Type as AsmType

/**
 * Class representing a type on the jvm.
 *
 * @param name       The simple java name of the type, e.g. "java.lang.Class" or "void".
 * @param descriptor The internal descriptor of the type on the jvm, e.g. "Ljava/lang/Class;" or "V".
 * @param signature  The internal signature of the type on the jvm, e.g. "Ljava/lang/Class<Ljava/lang/Object;>;" or "V".
 */
data class Type @JvmOverloads constructor(
        val name: String,
        val descriptor: String,
        val signature: String = descriptor
) {
    fun qualifiedName() = QualifiedName.fromQualifiedString(name)

    internal val asm: AsmType get() = AsmType.getType(descriptor)

    companion object {
        /**
         * Creates a type from a class with the given [qualifiedName] with generic parameters [generics].
         */
        fun clazz(
                qualifiedName: QualifiedName,
                generics: List<Type> = emptyList()
        ): Type {
            val params = if (generics.isEmpty()) "" else generics.joinToString("", "<", ">") { it.signature }
            val descriptorName = qualifiedName.toPathString()
            return Type(
                    qualifiedName.toQualifiedString(),
                    "L$descriptorName;",
                    "L$descriptorName$params;"
            )
        }

        /**
         * Creates a type from a class [clazz] with generic parameters [generics].
         */
        fun clazz(
                clazz: java.lang.Class<*>,
                generics: List<Type> = emptyList()
        ) = clazz(QualifiedName.fromClass(clazz), generics)

        /**
         * Creates a generic type with a given [name].
         */
        fun generic(name: String) = Type(name, "T$name;")

        /**
         * Creates the type representing an array of [type]s.
         */
        fun array(type: Type) = Type("${type.name}[]", "[${type.descriptor}")

        val `object` = clazz(Object::class.java)
        val string = clazz(String::class.java)
        val void = Type("void", "V")
        val boolean = Type("boolean", "Z")
        val char = Type("char", "C")
        val byte = Type("byte", "B")
        val short = Type("short", "S")
        val int = Type("int", "I")
        val long = Type("long", "J")
        val float = Type("float", "F")
        val double = Type("double", "D")
    }
}