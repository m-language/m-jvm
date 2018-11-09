package io.github.m

import java.lang.reflect.InvocationTargetException

/**
 * The runtime for top level M expressions.
 */
object Runtime {
    /**
     * The implementation of the main def for an M program with top level
     * expressions.
     *
     * @param args  The array of arguments passed to the program.
     * @param clazz The class to run.
     */
    @Suppress("unused")
    @JvmStatic
    fun run(args: Array<String>, clazz: Class<*>) {
        Definitions.args = MList.valueOf(args.map(String::mString))

        try {
            clazz.getMethod("run")(null)
        } catch (e: InvocationTargetException) {
            val cause = e.cause
            when (cause) {
                is MError -> throw cause
                else -> throw MError.Internal(cause)
            }
        } catch (t: Throwable) {
            throw MError.Internal(t)
        }
    }

    object Definitions {
        /**
         * The list of arguments passed to an M program. This value should only
         * be set once when [Runtime.run] is called.
         */
        @MField("args")
        @JvmField
        var args: MAny = MUndefined
    }
}