package io.github.m.asm

import io.github.m.MAny
import io.github.m.MSymbol
import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter

/**
 * An operation for a def.
 */
interface Operation : MAny {
    override val type get() = Companion.type

    fun generate(generatorAdapter: GeneratorAdapter)

    companion object : MAny {
        override val type get() = MSymbol("operation")

        val empty = Operation { }

        operator fun invoke(fn: GeneratorAdapter.() -> Unit) = object : Operation {
            override fun generate(generatorAdapter: GeneratorAdapter) = generatorAdapter.fn()
            override fun toString() = "operation"
        }
    }
}