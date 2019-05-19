package io.github.m

/**
 * The entry point for the M compiler, which takes an input file and an output
 * file as arguments.
 */
object Compiler {
    @JvmStatic
    fun main(args: kotlin.Array<String>) {
        if (args.size != 2) {
            System.err.println("Usage: mc <input> <output>")
        } else {
            val `in` = File(args[0])
            val out = File(args[1])
            try {
                compile(`in`, out)
            } catch (e: Generator.Failure) {
                System.err.println(e.message)
            }
        }
    }

    fun compile(`in`: File, out: File) {
        val exprs = Parser.parse(`in`, "", true).asCons()
        val env = Generator.Env(emptyMap(), emptyMap(), "", 0)
        val result = Generator.generate(exprs, env)
        Generator.writeProgram(out, result.operation, result.declarations)
    }
}
