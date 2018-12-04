package io.github.m

import java.io.File

/**
 * The entry point for the M compiler, which takes an input file and an output
 * file as arguments.
 */
@ExperimentalUnsignedTypes
object Compiler {
    @JvmStatic
    fun main(args: Array<String>) = compile(File(args[0]), File(args[1]))

    private fun compile(`in`: File, out: File) {
        val text = Seq.valueOf(`in`.readLines().joinToString("\n", "", "").asSequence())
        val exprs = Parser.parse(text)
        Generator.generate(`in`.nameWithoutExtension, File(out), exprs)
    }
}