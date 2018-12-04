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

    /**
     * M list definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("nil")
        @JvmField
        val nil: Value = List.Nil

        @MField("nil?")
        @JvmField
        val isNil: Value = Function { arg -> Bool(arg === List.Nil) }

        @MField("cons")
        @JvmField
        val cons: Value = Function { car, cdr -> List.Cons(car, cdr as List) }

        @MField("car")
        @JvmField
        val car: Value = Function { arg -> (arg as List.Cons).car }

        @MField("cdr")
        @JvmField
        val cdr: Value = Function { arg -> (arg as List.Cons).cdr }
    }
}