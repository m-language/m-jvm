package io.github.m

/**
 * Class representing an M pair.
 */
interface Pair : Value {
    /**
     * The left value of the pair.
     */
    val left: Value

    /**
     * The right value of the pair.
     */
    val right: Value

    /**
     * Default implementation of a pair.
     */
    data class Impl(override val left: Value, override val right: Value) : Pair {
        override fun toString() = "($left, $right)"
    }
}