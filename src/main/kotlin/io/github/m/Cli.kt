@file:JvmName("Cli")

package io.github.m

/**
 * The implementation of the main definition for an M program.
 *
 * @param args  The array of arguments passed to the program.
 * @param clazz The class to run.
 */
@Suppress("unused")
fun run(args: Array<String>, clazz: Class<*>) {
    try {
        val function = clazz.getField(Symbol.normalize("")).get(null) as? Value
                ?: throw Exception("Could not find main function")
        val value = function(args.asSequence().map { Symbol.toList(it) }.list())
        if (value is Error) throw value
        val process = value as? Process
                ?: throw Exception("Main must create a process (found ${value::class.java.name})")
        val result = process.run()
        if (result is Error) throw result
    } catch (e: Throwable) {
        e.stackTrace = e.stackTrace
                .map {
                    tailrec fun clean(name: String): String = if (name.contains("_"))
                        clean(name.substringBefore("_"))
                    else
                        name
                    StackTraceElement(it.className, Symbol.unnormalize(clean(it.methodName)), it.fileName, it.lineNumber)
                }
                .toTypedArray()
        throw e
    }
    System.out.flush()
}