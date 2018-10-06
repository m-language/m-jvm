package io.github.m

import kotlin.coroutines.experimental.buildIterator

/**
 * M implementation of lists.
 */
sealed class MList : MAny, Iterable<MAny> {
    /**
     * The head of the list.
     */
    abstract val car: MAny

    /**
     * The tail of the list.
     */
    abstract val cdr: MList

    override fun iterator() = buildIterator {
        var list = this@MList
        while (list != Nil) {
            yield(list.car)
            list = Cast.toList(list.cdr)
        }
    }

    override fun toString() = joinToString(" ", "(", ")")

    /**
     * The non empty list.
     */
    abstract class Cons : MList() {
        override val type get() = Companion.type

        companion object : MAny {
            override val type = MKeyword("cons")

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
        override val type = MKeyword("nil")
        override val car get() = throw MError.InvalidArgument("car", MList.Nil)
        override val cdr get() = throw MError.InvalidArgument("cdr", MList.Nil)
    }

    companion object : MAny {
        override val type = MKeyword("list")

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
        val cons: MAny = MFunction { car, cdr -> Cons(car, Cast.toList(cdr)) }

        @MField("car")
        @JvmField
        val car: MAny = MFunction { arg -> Cast.toList(arg).car }

        @MField("cdr")
        @JvmField
        val cdr: MAny = MFunction { arg -> Cast.toList(arg).cdr }
    }
}
