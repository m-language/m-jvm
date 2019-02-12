package io.github.m

import io.github.m.List as MList

/**
 * Class representing an M expression.
 */
sealed class Expr {
    abstract val path: String
    abstract val start: Position
    abstract val end: Position

    data class Identifier(val name: String, override val path: String, override val start: Position, override val end: Position) : Expr() {
        override fun toString() = name.replace("\r", "\\r").replace("\n", "\\n")
    }

    data class List(val exprs: kotlin.collections.List<Expr>, override val path: String, override val start: Position, override val end: Position) : Expr() {
        override fun toString() = exprs.joinToString(" ", "(", ")")
    }
}