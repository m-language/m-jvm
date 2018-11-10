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

    final override fun toString() = joinToString(" ", "(", ")")

    /**
     * The non empty list.
     */
    data class Cons(val car: Value, val cdr: List) : List() {
        override val type get() = Companion.type

        val cadr: Value get() = cdr.asCons.car
        val caddr: Value get() = cdr.asCons.cadr
        val cadddr: Value get() = cdr.asCons.caddr

        companion object : Value {
            override val type = Symbol("cons")
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

        fun valueOf(sequence: Sequence<Value>) = sequence
                .toList()
                .foldRight(Nil as List, ::Cons)
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