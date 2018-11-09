package io.github.m.asm

import jdk.internal.org.objectweb.asm.ClassWriter

/**
 * Class representing a field on the jvm.
 *
 * @param access    The access level of the field.
 * @param fieldType The type of the field.
 * @param name      The name of the field.
 * @param owner     The type of the owner of the field.
 */
data class Field(
        val access: Access,
        val fieldType: Type,
        val name: String,
        val owner: Type
) : Declaration {
    override fun generate(classWriter: ClassWriter) {
        val fieldVisitor = classWriter.visitField(
                access.intValue,
                name,
                fieldType.descriptor,
                fieldType.signature,
                null
        )

        fieldVisitor.visitEnd()
    }
}