package io.github.m

object Stdio {
    object Stdout : Value {
        override fun invoke(arg: Value) = Process { System.out.write(Char.from(arg).value.toInt()); arg }
    }

    object Stdin : Process {
        override fun run() = run {
            System.out.flush()
            Char(System.`in`.read().toChar())
        }
    }

    /**
     * M stdio definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("stdout")
        @JvmField
        val stdout: Value = Stdout

        @MField("stdin")
        @JvmField
        val stdin: Value = Stdin
    }
}