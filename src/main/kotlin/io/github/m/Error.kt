package io.github.m

/**
 * The superclass of all errors in M.
 */
sealed class Error(message: String) : java.lang.Error(message) {
    init {
        stackTrace = stackTrace
                .filter { it?.fileName?.contains(".m") ?: false }
                .map {
                    tailrec fun clean(name: String): String = if (name.contains("_"))
                        clean(name.substringBefore("_"))
                    else
                        name
                    StackTraceElement(it.className, clean(it.methodName), it.fileName, it.lineNumber)
                }
                .toTypedArray()
    }

    final override fun toString() = message ?: "null"

    @Suppress("unused")
    object Definitions {
        @MField("error")
        @JvmField
        val error: Value = Function { message ->
            throw Generic(message.asString)
        }
    }

    /**
     * Error that is thrown when no other error types match.
     */
    class Generic(message: String) : Error(message)

    /**
     * Error that is thrown when an M object is cast to an incompatible type.
     *
     * @param from The type of the object that is being cast.
     * @param to   The type that the object is being cast to.
     */
    data class Cast(val from: Symbol, val to: Symbol) : Error("Cannot cast $from to $to")

    /**
     * Error that is thrown when accessing a field that does not exist.
     *
     * @param key  The key of the field.
     * @param type The type of the object missing the field.
     */
    data class NoField(val key: Symbol, val type: Symbol) : Error("No field \"$key\" for $type")

    /**
     * Error that is thrown when a function is called with invalid arguments.
     *
     * @param function The name of the function.
     * @param arg      The argument of the function.
     */
    data class InvalidArgument(val function: String, val arg: Value) : Error("($function $arg)")

    /**
     * Error that is thrown when there is an error outside of the M program.
     */
    class Internal(message: String, cause: Throwable?) : Error(message) {
        constructor(cause: Throwable?) : this(cause?.message ?: "null", cause)

        init {
            initCause(cause)
        }
    }
}