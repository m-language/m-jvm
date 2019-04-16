package io.github.m

/**
 * Class representing an M pair.
 */
data class Pair(val first: Value, val second: Value) : Value {
    override fun invoke(arg: Value): Value = when (arg) {
        Bool.True -> first
        Bool.False -> second
        else -> arg(first, second)
    }

    companion object {
        fun from(value: Value) = value as? Pair ?: Pair(value(Bool.True), value(Bool.False))
    }

    /**
     * M pair definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("pair")
        @JvmField
        val pair: Value = Value { first, second -> Pair(first, second) }

        @MField("first")
        @JvmField
        val first: Value = Value { pair -> from(pair).first }

        @MField("second")
        @JvmField
        val second: Value = Value { pair -> from(pair).second }
    }
}