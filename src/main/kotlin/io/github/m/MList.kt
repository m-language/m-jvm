package io.github.m

/**
 * M implementation of lists.
 */
sealed class MList : MAny, Iterable<MAny> {
    override fun iterator() = run {
        var list = this@MList
        generateSequence {
            (list as? Cons)?.run {
                list = cdr.asList
                car
            }
        }.iterator()
    }

    override fun toString() = joinToString(" ", "(", ")")

    /**
     * The non empty list.
     */
    abstract class Cons : MList() {
        override val type get() = Companion.type

        /**
         * The head of the list.
         */
        abstract val car: MAny

        /**
         * The tail of the list.
         */
        abstract val cdr: MList

        val cadr: MAny get() = cdr.asCons.car
        val caddr: MAny get() = cdr.asCons.cadr
        val cadddr: MAny get() = cdr.asCons.caddr

        companion object : MAny {
            override val type = MSymbol("cons")

            operator fun invoke(car: MAny, cdr: MList) = Eager(car, cdr)
            operator fun invoke(lazyCons: () -> Cons) = Lazy(lazyCons)
        }

        class Eager(override val car: MAny, override val cdr: MList) : Cons()

        class Lazy(lazyCons: () -> Cons) : Cons() {
            private val cons by lazy(lazyCons)
            override val car get() = cons.car
            override val cdr get() = cons.cdr
        }
    }

    /**
     * The empty list.
     */
    object Nil : MList() {
        override val type = MSymbol("nil")
    }

    companion object : MAny {
        override val type = MSymbol("list")

        fun valueOf(sequence: Sequence<MAny>) = valueOf(sequence.iterator())
        fun valueOf(iterable: Iterable<MAny>) = valueOf(iterable.iterator())
        fun valueOf(iterator: Iterator<MAny>): MList = when {
            iterator.hasNext() -> Cons { Cons(iterator.next(), valueOf(iterator)) }
            else -> Nil
        }
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("nil")
        @JvmField
        val nil: MAny = Nil

        @MField("cons")
        @JvmField
        val cons: MAny = MFunction { car, cdr -> Cons(car, cdr.asList) }

        @MField("car")
        @JvmField
        val car: MAny = MFunction { arg -> arg.asCons.car }

        @MField("cdr")
        @JvmField
        val cdr: MAny = MFunction { arg -> arg.asCons.cdr }
    }
}