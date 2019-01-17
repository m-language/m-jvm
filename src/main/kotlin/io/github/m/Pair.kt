package io.github.m

/**
 * Class representing an M pair.
 */
interface Pair : List {
    /**
     * The left value of the pair.
     */
    val left: Value

    /**
     * The right value of the pair.
     */
    val right: Value

    override fun invoke(arg: Value): Value = when (arg) {
        Bool.True -> left
        Bool.False -> right
        else -> arg(left, right)
    }

    override fun iterator() = iterator {
        var list: Value = this@Pair
        while (list is Pair) {
            yield(list.left)
            list = list.right
        }
        yieldAll((list as List).iterator())
    }

    /**
     * Default implementation of a pair.
     */
    data class Impl(override val left: Value, override val right: Value) : Pair {
        override fun toString() = "($left, $right)"
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
        val pair: Value = Value { left, right -> Pair.Impl(left, right) }

        @MField("left")
        @JvmField
        val left: Value = Value { pair -> Pair.from(pair).left }

        @MField("right")
        @JvmField
        val right: Value = Value { pair -> Pair.from(pair).right }
    }
}