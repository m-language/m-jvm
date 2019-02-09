package io.github.m

/**
 * Class representing an M pair.
 */
interface Pair : List {
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

    override fun iterator() = iterator {
        var list: Value = this@Pair
        while (list is Pair) {
            yield(list.first)
            list = list.second
        }
        yieldAll((list as List).iterator())
    }

    /**
     * Default implementation of a pair.
     */
    data class Impl(override val first: Value, override val second: Value) : Pair {
        override fun toString() =
                if (all { it is Char })
                    joinToString(prefix = "", postfix = "", separator = "")
                else
                    joinToString(prefix = "(", postfix = ")", separator = " ")
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