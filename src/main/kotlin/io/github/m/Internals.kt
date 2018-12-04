package io.github.m

import java.lang.reflect.InvocationTargetException

/**
 * Internal definitions used by the M runtime.
 */
@Suppress("unused")
object Internals {
    /**
     * Converts an M [bool] to a java bool.
     */
    @JvmStatic
    fun toPrimitiveBool(bool: Value) = bool as Bool === Bool.True

    /**
     * The singleton empty list.
     */
    @JvmField
    val nil: Value = List.Nil

    /**
     * Applies an M [function] to its [arg].
     */
    @JvmStatic
    fun apply(function: Value, arg: Value) = (function as Function)(arg)

    /**
     * The list of arguments passed to an M program. This value should only
     * be set once when [Runtime.run] is called.
     */
    @MField("args")
    @JvmField
    var args: Value = List.Nil

    /**
     * The implementation of the main definition for an M program with top level
     * expressions.
     *
     * @param args  The array of arguments passed to the program.
     * @param clazz The class to run.
     */
    @JvmStatic
    fun run(args: Array<String>, clazz: Class<*>) {
        this.args = List.valueOf(args.asSequence().map(String::toList))

        try {
            try {
                clazz.getMethod("run")(null)
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }
        } catch (e: Throwable) {
            e.stackTrace = e.stackTrace
                    .map {
                        tailrec fun clean(name: String): String = if (name.contains("_"))
                            clean(name.substringBefore("_"))
                        else
                            name
                        StackTraceElement(it.className, clean(it.methodName), it.fileName, it.lineNumber)
                    }
                    .filterNot { it.className?.contains("io.github.m") ?: false }
                    .dropLast(1)
                    .dropLastWhile { it.fileName?.contains(".m")?.not() ?: true }
                    .toTypedArray()
            throw e
        }
    }
}