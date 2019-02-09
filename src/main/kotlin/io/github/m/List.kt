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

    object Nil : List() {
        override fun invoke(arg: Value) = Bool.False(arg)
        override fun toString() = "()"
    }

    data class Cons(val car: Value, val cdr: List) : List() {
        override fun invoke(arg: Value) = Pair.Impl(car, cdr)(arg)
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
        @Suppress("RedundantCompanionReference")
        @MField("nil")
        @JvmField
        val nil: Value = Nil

        @MField("nil?")
        @JvmField
        val isNil: Value = Value { arg -> arg(Value { _, _, _ -> Bool.False }, Bool.True) }

        @MField("cons")
        @JvmField
        val cons: Value = Value { car, cdr -> Cons(car, List.from(cdr)) }

        @MField("car")
        @JvmField
        val car: Value = Value { arg -> arg(Bool.True) }

        @MField("cdr")
        @JvmField
        val cdr: Value = Value { arg -> arg(Bool.False) }
    }
}