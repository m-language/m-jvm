package io.github.m

import java.lang.Exception

/**
 * Internal definitions used by the M runtime.
 */
@Suppress("unused")
object Internals {
    /**
     * Converts a [io.github.m.Bool] to a [java.lang.Boolean].
     */
    @JvmStatic
    fun toPrimitiveBool(bool: Value) = Bool.from(bool) === Bool.True

    /**
     * The singleton empty list.
     */
    @JvmField
    val nil: Value = List.nil

    /**
     * Creates a process which always returns [value].
     */
    @JvmStatic
    fun `do`(value: Value): Value = Process.Do(value)

    /**
     * The implementation of the main definition for an M program.
     *
     * @param args  The array of arguments passed to the program.
     * @param clazz The class to run.
     */
    @JvmStatic
    fun run(args: Array<String>, clazz: Class<*>) {
        try {
            val function = clazz.getField("").get(null) as? Value ?: throw Exception("Could not find main function")
            val process = function(List.valueOf(args.asSequence().map(String::toList))) as? Process ?: throw Exception("Main must create a process")
            process.run()
        } catch (e: Throwable) {
            e.stackTrace = e.stackTrace
                    .map {
                        tailrec fun clean(name: String): String = if (name.contains("_"))
                            clean(name.substringBefore("_"))
                        else
                            name
                        StackTraceElement(it.className, clean(it.methodName), it.fileName, it.lineNumber)
                    }
//                    .dropLast(1)
//                    .dropLastWhile { it.fileName?.contains(".m")?.not() ?: true }
                    .toTypedArray()
            throw e
        }
        System.out.flush()
    }
}