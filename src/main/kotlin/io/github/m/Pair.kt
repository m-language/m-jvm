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

    /**
     * M pair definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("pair")
        @JvmField
        val pair: Value = Function { left, right -> Pair.Impl(left, right) }

        @MField("left")
        @JvmField
        val left: Value = Function { pair -> (pair as Pair).left }

        @MField("right")
        @JvmField
        val right: Value = Function { pair -> (pair as Pair).right }
    }
}