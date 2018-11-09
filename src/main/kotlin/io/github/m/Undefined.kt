package io.github.m

/**
 * The singleton object which represents a value that is not yet defined.
 */
object Undefined : Value {
    override val type = Symbol("undefined")

    @Suppress("unused")
    object Definitions {
        @MField("undefined")
        @JvmField
        val undefined: Value = Undefined
    }
}