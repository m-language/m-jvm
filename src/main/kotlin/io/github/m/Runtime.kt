package io.github.m

import java.lang.reflect.InvocationTargetException

/**
 * The runtime for top level M expressions.
 */
object Runtime {
    /**
     * The implementation of the main definition for an M program with top level
     * expressions.
     *
     * @param args  The array of arguments passed to the program.
     * @param clazz The class to run.
     */
    @Suppress("unused")
    @JvmStatic
    fun run(args: Array<String>, clazz: Class<*>) {
        Definitions.args = List.valueOf(args.map(String::m))

        try {
            clazz.getMethod("run")(null)
        } catch (e: InvocationTargetException) {
            val cause = e.cause
            when (cause) {
                is Error -> throw cause
                else -> throw Error.Internal(cause)
            }
        } catch (t: Throwable) {
            throw Error.Internal(t)
        }
    }

    object Definitions {
        /**
         * The list of arguments passed to an M program. This value should only
         * be set once when [Runtime.run] is called.
         */
        @MField("args")
        @JvmField
        var args: Value = Undefined
    }
}