package io.github.m.asm

import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import jdk.internal.org.objectweb.asm.Opcodes.V1_8
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Class representing a class on the jvm.
 *
 * @param access      The access level of the class.
 * @param name        The name of the class.
 * @param signature   The signature of the class.
 * @param declaration The declaration in the class.
 */
data class Class(
        val access: Access,
        val name: QualifiedName,
        val signature: ClassSignature,
        val declaration: Declaration
) {
    constructor(
            access: Access,
            name: QualifiedName,
            generics: List<Generic>,
            superType: Type,
            interfaceTypes: Set<Type>,
            declaration: Declaration
    ) : this(access, name, ClassSignature(generics, superType, interfaceTypes), declaration)

    /**
     * Generates this class, returning a byte array that represents the class file for this class.
     */
    private fun generate(): ByteArray {
        val classWriter = ClassWriter(COMPUTE_FRAMES)

        classWriter.visit(
                V1_8,
                access.intValue,
                name.toPathString(),
                signature.internalString().let { if (it.isEmpty()) null else it },
                signature.superType.qualifiedName().toPathString(),
                signature.interfaceTypes.map { it.qualifiedName().toPathString() }.toTypedArray()
        )

        declaration.generate(classWriter)

        classWriter.visitEnd()
        return classWriter.toByteArray()
    }

    /**
     * Writes the bytes of this class to a file in the given [directory].
     */
    fun generate(directory: File): File {
        val file = File(directory, "${name.toPathString()}.class")
        val path = file.toPath()
        file.parentFile.mkdirs()
        if (file.exists()) Files.delete(path)
        Files.write(path, generate(), StandardOpenOption.CREATE)
        return file
    }
}