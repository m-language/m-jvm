package io.github.m

import io.github.m.List as MList

/**
 * Class representing an M expression.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
sealed class Expr : Value {
    abstract val line: Nat

    data class Identifier(val name: MList, override val line: Nat) : Expr(), Data {
        constructor(name: Sequence<kotlin.Char>, line: kotlin.UInt) : this(MList.valueOf(name.map { Char(it) }), Nat(line))

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

    data class List(val exprs: MList, override val line: Nat) : Expr(), Data {
        constructor(exprs: Sequence<Expr>, line: kotlin.UInt) : this(MList.valueOf(exprs), Nat(line))

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
        val identifierExpr: Value = Function { name, line ->
            Identifier(name.asList, line.asNat)
        }

        @MField("list-expr")
        @JvmField
        val listExpr: Value = Function { exprs, line ->
            List(exprs.asList, line.asNat)
        }
    }
}