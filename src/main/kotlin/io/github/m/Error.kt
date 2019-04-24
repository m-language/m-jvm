package io.github.m

/**
 * M wrapper class for errors.
 */
class Error(message: String) : Throwable(message), Value {
    override fun invoke(arg: Value) = this
    override fun toString() = "Error: $message"

    @Suppress("unused")
    object Definitions {
        @MField("error")
        @JvmField
        val error: Value = Value.Impl1 { arg -> Error(List.from(arg).toString) }
    }
}