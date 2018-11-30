package io.github.m

/**
 * M bool definitions.
 */
@Suppress("unused")
object Bools {
    @MField("true")
    @JvmField
    val `true`: Value = Bool.True

    @MField("false")
    @JvmField
    val `false`: Value = Bool.False
}