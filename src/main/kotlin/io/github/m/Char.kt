package io.github.m

/**
 * M wrapper class for chars
 */
data class Char(val value: kotlin.Char) : Value {
    override fun toString() = value.toString()
}