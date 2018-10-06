package io.github.m.asm

import jdk.internal.org.objectweb.asm.ClassWriter

/**
 * A declaration for a class.
 */
interface Declaration {
    fun generate(classWriter: ClassWriter)
}