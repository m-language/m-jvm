package io.github.m

/**
 * M implementation of lists.
 */
sealed class List : Iterable<Value>, Value {
    override fun iterator() = run {
        var list = this@List
        generateSequence {
            (list as? Cons)?.run {
                list = cdr
                car
            }
        }.iterator()
    }

    final override fun toString() = joinToString(" ", "(", ")")

    /**
     * The non empty list.
     */
    data class Cons(val car: Value, val cdr: List) : List(), Pair {
        override val left get() = car
        override val right get() = cdr
    }

    /**
     * The empty list.
     */
    object Nil : List()

    companion object {
        fun valueOf(sequence: Sequence<Value>) = sequence
                .toList()
                .foldRight(Nil as List, ::Cons)
    }
}