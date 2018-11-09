package io.github.m.asm

import io.github.m.Symbol
import io.github.m.Value
import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter

/**
 * An operation for a method.
 */
interface Operation : Value {
    override val type get() = Companion.type

    fun generate(generatorAdapter: GeneratorAdapter)

    companion object : Value {
        override val type get() = Symbol("operation")

        val empty = Operation { }

        operator fun invoke(fn: GeneratorAdapter.() -> Unit) = object : Operation {
            override fun generate(generatorAdapter: GeneratorAdapter) = generatorAdapter.fn()
            override fun toString() = "operation"
        }
    }
}