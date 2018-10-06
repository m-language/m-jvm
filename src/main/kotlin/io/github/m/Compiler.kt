package io.github.m

import java.io.File

/**
 * The entry point for the M compiler, which takes an input file and an output
 * file as arguments.
 */
@SelfHosted
object Compiler {
    @JvmStatic
    fun main(args: Array<String>) = compile(File(args[0]), File(args[1]))

    fun compile(`in`: File, out: File) {
        val text = `in`.readLines().joinToString("\n", "", "").asSequence()
        val exprs = (Parser.parser(text, 1) as Parser.Result.Success).result
        val clazz = Gen.gen(exprs, `in`)
        clazz.generate(out)
    }
}