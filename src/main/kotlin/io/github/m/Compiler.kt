package io.github.m

import java.io.File

/**
 * The entry point for the M compiler, which takes an input file and an output
 * file as arguments.
 */
object Compiler {
    @JvmStatic
    fun main(args: Array<String>) = compile(File(args[0]), File(args[1]))

    private fun compile(`in`: File, out: File) {
        val text = `in`.readLines().joinToString("\n", "", "").asSequence()
        val exprs = (Parser.parser(text, 1) as Parser.Result.Success).value
        Generator.Definitions.generate.asFunction(`in`.nameWithoutExtension.mString, MFile(out), MList.valueOf(exprs))
                .asProcess()
    }
}