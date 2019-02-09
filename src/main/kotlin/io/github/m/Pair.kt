package io.github.m

/**
 * Class representing an M pair.
 */
interface Pair : Value {
    /**
     * The first value of the pair.
     */
    val first: Value

    /**
     * The second value of the pair.
     */
    val second: Value

    override fun invoke(arg: Value): Value = when (arg) {
        Bool.True -> first
        Bool.False -> second
        else -> arg(first, second)
    }

    /**
     * Default implementation of a pair.
     */
    data class Impl(override val first: Value, override val second: Value) : Pair {
        override fun toString() = "($first, $second)"
    }

    companion object {
        fun from(value: Value) = value as? Pair ?: Impl(value(Bool.True), value(Bool.False))
    }

    /**
     * M pair definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("pair")
        @JvmField
        val pair: Value = Value { first, second -> Pair.Impl(first, second) }

        @MField("first")
        @JvmField
        val first: Value = Value { pair -> from(pair).first }

        @MField("second")
        @JvmField
        val second: Value = Value { pair -> from(pair).second }
    }
}