package io.github.m

/**
 * The singleton object which represents the value of the following code:
 *
 * ```m
 * (def undefined)
 * ```
 */
object MUndefined : MAny {
    override val type = MKeyword("undefined")

    @Suppress("unused")
    object Definitions {
        /**
         * Internal definition to prevent re-defining undefined.
         */
        @MField("undefined")
        @JvmField
        val undefined = MUndefined
    }
}