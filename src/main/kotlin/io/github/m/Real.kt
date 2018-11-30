package io.github.m

/**
 * M wrapper class for reals.
 */
data class Real(val value: Float) : Value {
    override fun toString() = value.toString()
}