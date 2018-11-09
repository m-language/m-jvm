package io.github.m

/**
 * M implementation of lists.
 */
sealed class List : Value, Iterable<Value> {
    override fun iterator() = run {
        var list = this@List
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
    abstract class Cons : List() {
        override val type get() = Companion.type

        /**
         * The head of the list.
         */
        abstract val car: Value

        /**
         * The tail of the list.
         */
        abstract val cdr: List

        val cadr: Value get() = cdr.asCons.car
        val caddr: Value get() = cdr.asCons.cadr
        val cadddr: Value get() = cdr.asCons.caddr

        companion object : Value {
            override val type = Symbol("cons")

            operator fun invoke(car: Value, cdr: List) = Eager(car, cdr)
            operator fun invoke(lazyCons: () -> Cons) = Lazy(lazyCons)
        }

        class Eager(override val car: Value, override val cdr: List) : Cons()

        class Lazy(lazyCons: () -> Cons) : Cons() {
            private val cons by lazy(lazyCons)
            override val car get() = cons.car
            override val cdr get() = cons.cdr
        }
    }

    /**
     * The empty list.
     */
    object Nil : List() {
        override val type = Symbol("nil")
    }

    companion object : Value {
        override val type = Symbol("list")

        fun valueOf(sequence: Sequence<Value>) = valueOf(sequence.iterator())
        fun valueOf(iterable: Iterable<Value>) = valueOf(iterable.iterator())
        fun valueOf(iterator: Iterator<Value>): List = when {
            iterator.hasNext() -> Cons { Cons(iterator.next(), valueOf(iterator)) }
            else -> Nil
        }
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("nil")
        @JvmField
        val nil: Value = Nil

        @MField("cons")
        @JvmField
        val cons: Value = Function { car, cdr -> Cons(car, cdr.asList) }

        @MField("car")
        @JvmField
        val car: Value = Function { arg -> arg.asCons.car }

        @MField("cdr")
        @JvmField
        val cdr: Value = Function { arg -> arg.asCons.cdr }
    }
}