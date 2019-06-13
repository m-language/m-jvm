package io.github.m

import io.github.m.List as MList

/**
 * Class representing an M expression.
 */
sealed class Expr {
    data class Symbol(val name: String) : Expr() {
        override fun toString() = name.replace("\r", "\\r").replace("\n", "\\n")
    }

    data class List(val exprs: kotlin.collections.List<Expr>) : Expr() {
        override fun toString() = exprs.joinToString(" ", "(", ")")
    }
}