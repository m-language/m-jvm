package io.github.m

/**
 * Class representing an M compare.
 */
sealed class Compare : Data {
    object Equals : Compare() {
        override val type = Symbol("compare=")
        override fun get(key: Symbol) = noField(key)
        override fun toString() = "="
    }

    object Less : Compare() {
        override val type = Symbol("compare<")
        override fun get(key: Symbol) = noField(key)
        override fun toString() = "<"
    }

    object Greater : Compare() {
        override val type = Symbol("compare>")
        override fun get(key: Symbol) = noField(key)
        override fun toString() = ">"
    }

    companion object {
        fun list(compare: Function) = Function { a, b ->
            tailrec fun impl(a: List, b: List): Compare = when {
                a is List.Nil && b is List.Nil -> Equals
                a is List.Nil -> Less
                b is List.Nil -> Greater
                else -> {
                    a as List.Cons
                    b as List.Cons
                    val result = compare(a.car, b.car).cast<Compare>()
                    if (result == Equals) impl(a.cdr, b.cdr) else result
                }
            }

            impl(a.asList, b.asList)
        }

        fun from(comparator: Comparator<Value>) = Function { a, b ->
            val compare = comparator.compare(a, b)
            when {
                compare < 0 -> Less
                compare > 0 -> Greater
                else -> Equals
            }
        }
    }

    @Suppress("unused")
    object Definitions {
        @MField("compare=")
        @JvmField
        val equals = Equals

        @MField("compare<")
        @JvmField
        val less = Less

        @MField("compare>")
        @JvmField
        val greater = Greater
    }
}
