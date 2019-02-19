package io.github.m

/**
 * The entry point for the M compiler, which takes an input file and an output
 * file as arguments.
 */
@ExperimentalUnsignedTypes
object Compiler {
    @JvmStatic
    fun main(args: Array<String>) {
        if(args.size != 2){
            System.err.println("Usage: mc <input> <output>")
            return;
        }
        val `in` = File(args[0])
        val out = File(args[1])
        Generator.generate(`in`, out)
    }
}
