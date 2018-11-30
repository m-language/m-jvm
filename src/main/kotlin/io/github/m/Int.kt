package io.github.m

/**
 * M wrapper class for ints.
 */
data class Int(val value: kotlin.Int) : Value {
    override fun toString() = value.toString()
}