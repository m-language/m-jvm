package io.github.m

import io.github.m.List as MList

/**
 * Class representing an M expression.
 */
@ExperimentalUnsignedTypes
sealed class Expr : Data {
    abstract val line: Nat

    data class Identifier(val name: MList, override val line: Nat) : Expr() {
        constructor(name: Sequence<Char>, line: Nat) : this(MList.valueOf(name), line)
        override fun toString() = name.toString

        override val type = "identifier-expr"
        override fun get(name: String) = when (name) {
            "name"-> this.name
            "line" -> this.line
            else -> null
        }
    }

    data class List(val exprs: MList, override val line: Nat) : Expr() {
        constructor(exprs: Sequence<Expr>, line: Nat) : this(MList.valueOf(exprs), line)
        override fun toString() = exprs.joinToString(" ", "(", ")")

        override val type = "list-expr"
        override fun get(name: String) = when (name) {
            "exprs" -> this.exprs
            "line" -> this.line
            else -> null
        }
    }
}