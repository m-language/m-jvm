package io.github.m

/**
 * Class representing an M pair.
 */
data class Pair(val first: Value, val second: Value) : Value {
    override fun invoke(arg: Value): Value = when (arg) {
        Bool.TRUE -> first
        Bool.FALSE -> second
        else -> arg(first, second)
    }

    companion object {
        fun from(value: Value) = value as? Pair ?: Pair(value(Bool.TRUE), value(Bool.FALSE))
    }

    /**
     * M pair definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField(name = "pair")
        @JvmField
        val pair: Value = Value.Impl2 { first, second -> Pair(first, second) }

        @MField(name = "first")
        @JvmField
        val first: Value = Value.Impl1 { pair -> from(pair).first }

        @MField(name = "second")
        @JvmField
        val second: Value = Value.Impl1 { pair -> from(pair).second }
    }
}