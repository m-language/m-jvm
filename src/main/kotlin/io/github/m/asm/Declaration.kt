package io.github.m.asm

import io.github.m.Symbol
import io.github.m.Value
import jdk.internal.org.objectweb.asm.ClassWriter

/**
 * A declaration for a class.
 */
interface Declaration : Value {
    override val type get() = Operation.type

    fun generate(classWriter: ClassWriter)

    companion object : Value {
        override val type get() = Symbol("declaration")

        val empty = Declaration { }

        operator fun invoke(fn: ClassWriter.() -> Unit) = object : Declaration {
            override fun generate(classWriter: ClassWriter) = fn(classWriter)
            override fun toString() = "declaration"
        }
    }
}