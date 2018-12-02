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

    fun reverse() = List.valueOf(reversed().asSequence())

    /**
     * The non empty list.
     */
    data class Cons(val car: Value, val cdr: List) : List(), Pair {
        override val left get() = car
        override val right get() = cdr
        val cadr get() = (cdr as Cons).car
        val caddr get() = (cdr as Cons).cadr
        val cadddr get() = (cdr as Cons).caddr
        val cddr get() = (cdr as Cons).cdr
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