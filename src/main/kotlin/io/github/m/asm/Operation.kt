package io.github.m.asm

import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter

/**
 * An operation for a method.
 */
interface Operation {
    fun generate(generatorAdapter: GeneratorAdapter)

    companion object {
        operator fun invoke(fn: GeneratorAdapter.() -> Unit) = object : Operation {
            override fun generate(generatorAdapter: GeneratorAdapter) = generatorAdapter.fn()
        }
    }
}