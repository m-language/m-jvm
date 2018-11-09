package io.github.m

/**
 * Class representing an M compare.
 */
sealed class Compare : MData {
    object Equals : Compare() {
        override val type = MSymbol("compare=")
        override fun get(key: MSymbol) = noField(key)
        override fun toString() = "="
    }

    object Less : Compare() {
        override val type = MSymbol("compare<")
        override fun get(key: MSymbol) = noField(key)
        override fun toString() = "<"
    }

    object Greater : Compare() {
        override val type = MSymbol("compare>")
        override fun get(key: MSymbol) = noField(key)
        override fun toString() = ">"
    }

    companion object {
        fun list(compare: MFunction) = MFunction { a, b ->
            tailrec fun impl(a: MList, b: MList): Compare = when {
                a is MList.Nil && b is MList.Nil -> Equals
                a is MList.Nil -> Less
                b is MList.Nil -> Greater
                else -> {
                    a as MList.Cons
                    b as MList.Cons
                    val result = compare(a.car, b.car).cast<Compare>()
                    if (result == Equals) impl(a.cdr, b.cdr) else result
                }
            }

            impl(a.asList, b.asList)
        }

        fun from(comparator: Comparator<MAny>) = MFunction { a, b ->
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
