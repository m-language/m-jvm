package io.github.m

object Stdio {
    object Stdout : Value {
        override fun invoke(arg: Value) = Process { System.out.write(Char.from(arg).char.toInt()); List.NIL }
    }

    object Stderr : Value {
        override fun invoke(arg: Value) = Process { System.err.write(Char.from(arg).char.toInt()); List.NIL }
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
        @MField(name = "stdout")
        @JvmField
        val stdout: Value = Stdout

        @MField(name = "stderr")
        @JvmField
        val stderr: Value = Stderr

        @MField(name = "stdin")
        @JvmField
        val stdin: Value = Stdin
    }
}