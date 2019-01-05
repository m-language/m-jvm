package io.github.m

/**
 * M implementation of lists.
 */
interface List : Iterable<Value>, Function {
    companion object {
        val nil = Bool.False

        fun cons(a: Value, b: List) = Pair.Impl(a, b)

        fun from(value: Value): List = value as? List ?: (value as Function)(Function { a, b, _ -> cons(a, from(b)) }, nil) as List

        fun valueOf(sequence: Sequence<Value>) = sequence
                .toList()
                .foldRight(nil as List, ::cons)
    }

    /**
     * M list definitions.
     */
    @Suppress("unused")
    object Definitions {
        @Suppress("RedundantCompanionReference")
        @MField("nil")
        @JvmField
        val nil: Value = Companion.nil

        @MField("nil?")
        @JvmField
        val isNil: Value = Function { arg -> (arg as Function)(Function { _, _, _ -> Bool.False }, Bool.True) }

        @MField("cons")
        @JvmField
        val cons: Value = Function { car, cdr -> List.cons(car, List.from(cdr)) }

        @MField("car")
        @JvmField
        val car: Value = Function { arg -> (arg as Function)(Bool.True) }

        @MField("cdr")
        @JvmField
        val cdr: Value = Function { arg -> (arg as Function)(Bool.False) }
    }
}