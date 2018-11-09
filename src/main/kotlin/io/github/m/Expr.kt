package io.github.m

import io.github.m.List as MList

/**
 * Class representing an M expression.
 */
sealed class Expr : Value {
    abstract val line: Int

    data class Identifier(val name: MList, override val line: Int) : Expr(), Data {
        constructor(name: Sequence<kotlin.Char>, line: kotlin.Int) : this(MList.valueOf(name.map { Char(it) }), Int(line))

        override val type get() = Companion.type

        override fun get(key: Symbol) = when (key.value) {
            "name" -> name
            "line" -> line
            else -> noField(key)
        }

        override fun toString() = name.asString

        companion object : Value {
            override val type = Symbol("identifier-expr")
        }
    }

    data class List(val exprs: MList, override val line: Int) : Expr(), Data {
        constructor(exprs: Sequence<Expr>, line: kotlin.Int) : this(MList.valueOf(exprs), Int(line))

        override val type get() = Companion.type

        override fun get(key: Symbol) = when (key.value) {
            "exprs" -> exprs
            "line" -> line
            else -> noField(key)
        }

        override fun toString() = exprs.joinToString(" ", "(", ")")

        companion object : Value {
            override val type = Symbol("list-expr")
        }
    }

    @Suppress("unused")
    object Definitions {
        @MField("identifier-expr")
        @JvmField
        val identifierExpr: Value = Function { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            Identifier(list.car.asList, list2.car.asInt)
        }

        @MField("list-expr")
        @JvmField
        val listExpr: Value = Function { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            List(list.car.asList, list2.car.asInt)
        }
    }
}