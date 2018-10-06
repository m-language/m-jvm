package io.github.m

import java.io.File

/**
 * The entry point for the M compiler, which takes an input file and an output
 * file as arguments.
 */
@SelfHosted
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = File(args[0])
        val text = input.readLines().joinToString("\n", "", "").asSequence()
        val exprs = (Parser.parser(text, 1) as Parser.Result.Success).result
        val clazz = Gen.gen(exprs, input)
        clazz.generate(File(args[1]))
    }
}