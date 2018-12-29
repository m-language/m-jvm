package io.github.m

import java.lang.Exception

/**
 * Internal definitions used by the M runtime.
 */
@Suppress("unused")
object Internals {
    /**
     * Converts [io.github.m.Bool] to [java.lang.Boolean].
     */
    @JvmStatic
    fun toPrimitiveBool(bool: Value) = bool as Bool === Bool.True

    /**
     * The singleton empty list.
     */
    @JvmField
    val nil: Value = List.Nil

    /**
     * Applies an M [function] to an [argument].
     */
    @JvmStatic
    fun apply(function: Value, argument: Value) = (function as Function)(argument)

    /**
     * The implementation of the main definition for an M program.
     *
     * @param args  The array of arguments passed to the program.
     * @param clazz The class to run.
     */
    @JvmStatic
    fun run(args: Array<String>, clazz: Class<*>) {
        try {
            val function = clazz.getField("").get(null) as? Function ?: throw Exception("Could not find main function")
            val process = function(List.valueOf(args.asSequence().map(String::toList))) as? Process ?: throw Exception("Main must create a process")
            process()
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