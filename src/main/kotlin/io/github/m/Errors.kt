package io.github.m

/**
 * M error definitions.
 */
@Suppress("unused")
object Errors {
    class Error(message: String) : Throwable(message), Value {
        override fun invoke(arg: Value) = this
        override fun toString() = "Error: $message"
    }

    @MField("error")
    @JvmField
    val error: Value = Value { arg -> Error(List.from(arg).toString) }
}