package io.github.m

/**
 * The superclass of all errors in M.
 */
sealed class MError(message: String) : java.lang.Error(message) {
    @Suppress("unused")
    object Definitions {
        @MField("error")
        @JvmField
        val error: MAny = MFunction { message ->
            throw Generic(io.github.m.Cast.toList(message).string)
        }
    }

    /**
     * Error that is thrown when no other error types match.
     */
    class Generic(message: String) : MError(message)

    /**
     * Error that is thrown when an M object is cast to an incompatible type.
     *
     * @param from The type of the object that is being cast.
     * @param to   The type that the object is being cast to.
     */
    data class Cast(val from: MSymbol, val to: MSymbol) : MError("Cannot cast $from to $to")

    /**
     * Error that is thrown when accessing a field that does not exist.
     *
     * @param key  The key of the field.
     * @param type The type of the object missing the field.
     */
    data class NoField(val key: MSymbol, val type: MSymbol) : MError("No field $key for $type")

    /**
     * Error that is thrown when a function is called with invalid arguments.
     *
     * @param function The name of the function.
     * @param arg      The argument of the function.
     */
    data class InvalidArgument(val function: String, val arg: MAny) : MError("($function $arg)")

    /**
     * Error that is thrown when there is an error outside of the M program.
     */
    class Internal(message: String, cause: Throwable?) : MError(message) {
        constructor(cause: Throwable?) : this(cause?.message ?: "null", cause)

        init {
            initCause(cause)
        }
    }
}