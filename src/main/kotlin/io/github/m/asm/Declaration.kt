package io.github.m.asm

import io.github.m.MAny
import io.github.m.MSymbol
import jdk.internal.org.objectweb.asm.ClassWriter

/**
 * A declaration for a class.
 */
interface Declaration : MAny {
    override val type
        get() = Operation.type

    fun generate(classWriter: ClassWriter)

    companion object : MAny {
        override val type get() = MSymbol("declaration")

        val empty = Declaration {  }

        operator fun invoke(fn: ClassWriter.() -> Unit) = object : Declaration {
            override fun generate(classWriter: ClassWriter) = fn(classWriter)
            override fun toString() = "declaration"
        }
    }
}