package io.github.m

/**
 * M implementation of lists.
 */
sealed class List : Iterable<Value>, Value {
    override fun iterator() = iterator {
        var list: List = this@List
        while (list is Cons) {
            yield(list.car)
            list = list.cdr
        }
    }

    object Nil : List(), Value.Delegate {
        override val value = Bool.False
        override fun toString() = "()"
    }

    data class Cons(val car: Value, val cdr: List) : List(), Value.Delegate {
        override val value get() = Pair(car, cdr)
        override fun toString() = joinToString(" ", "(", ")")
    }

    companion object {
        fun from(value: Value): List = value as? List ?: value(Value { a, b, _ -> Cons(a, from(b)) }, Nil) as List

        fun valueOf(sequence: Sequence<Value>) = sequence
                .toList()
                .foldRight(Nil as List) { a, b -> Cons(a, b) }
    }

    /**
     * M list definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("nil")
        @JvmField
        val nil: Value = Nil

        @MField("cons")
        @JvmField
        val cons: Value = Value { car, cdr -> Cons(car, List.from(cdr)) }
    }
}