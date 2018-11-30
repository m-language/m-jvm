package io.github.m

/**
 * M expr definitions.
 */
@Suppress("unused")
@ExperimentalUnsignedTypes
object Exprs {
    @MField("identifier-expr")
    @JvmField
    val identifier: Value = Function { name, line -> Expr.Identifier(name as List, line as Nat) }

    @MField("list-expr")
    @JvmField
    val list: Value = Function { exprs, line -> Expr.List(exprs as List, line as Nat) }
}