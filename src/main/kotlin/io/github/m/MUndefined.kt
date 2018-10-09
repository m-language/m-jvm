package io.github.m

/**
 * The singleton object which represents the value of the following code:
 *
 * ```m
 * (def undefined)
 * ```
 */
object MUndefined : MAny {
    override val type = MSymbol("undefined")

    @Suppress("unused")
    object Definitions {
        @MField("undefined")
        @JvmField
        val undefined = MUndefined
    }
}