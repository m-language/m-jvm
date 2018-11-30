package io.github.m

/**
 * M pair definitions.
 */
@Suppress("unused")
object Pairs {
    @MField("pair")
    @JvmField
    val pair: Value = Function { left, right -> Pair.Impl(left, right) }

    @MField("left")
    @JvmField
    val left: Value = Function { pair -> (pair as Pair).left }

    @MField("right")
    @JvmField
    val right: Value = Function { pair -> (pair as Pair).right }
}