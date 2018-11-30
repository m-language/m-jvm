package io.github.m

/**
 * M wrapper class for strings.
 */
@ExperimentalUnsignedTypes
data class Symbol(val value: String) : Value {
    override fun toString() = value
}