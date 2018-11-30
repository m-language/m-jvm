package io.github.m

/**
 * M error definitions.
 */
@Suppress("unused")
@ExperimentalUnsignedTypes
object Errors {
    @MField("error")
    @JvmField
    val error: Value = Function { arg -> throw Error((arg as Symbol).value) }
}