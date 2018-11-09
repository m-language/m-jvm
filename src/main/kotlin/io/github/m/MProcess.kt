package io.github.m

/**
 * M implementation of IO processes.
 */
@FunctionalInterface
interface MProcess : MAny {
    @JvmDefault
    override val type
        get() = Companion.type

    operator fun invoke(): MAny

    companion object : MAny {
        /**
         * The type of all processes.
         */
        override val type = MSymbol("process")

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: () -> MAny) = Impl(fn)
    }

    class Impl(val fn: () -> MAny) : MProcess {
        override fun invoke(): MAny = fn()
    }

    /**
     * Implementation of [Definitions.thenRun].
     */
    class ThenRun(val a: MProcess, val b: MProcess) : MProcess {
        override fun invoke(): MAny = runAll(this)

        private tailrec fun runAll(process: MProcess): MAny = when (process) {
            is ThenRun -> {
                process.a()
                runAll(process.b)
            }
            else -> process()
        }
    }

    /**
     * Implementation of [Definitions.runWith].
     */
    class RunWith(val process: MProcess, val function: MFunction) : MProcess {
        override fun invoke(): MAny = function(process())
    }

    /**
     * Implementation of [Definitions.thenRunWith].
     */
    class ThenRunWith(val process: MProcess, val function: MFunction) : MProcess {
        override fun invoke(): MAny = function(process()).asProcess()

        private tailrec fun rec(process: MProcess): MAny = when (process) {
            is ThenRunWith -> rec(function(process.process()).asProcess)
            else -> process()
        }
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("then-run")
        @JvmField
        val thenRun: MAny = MFunction { proc1, proc2 -> ThenRun(proc1.asProcess, proc2.asProcess) }

        @MField("run-with")
        @JvmField
        val runWith: MAny = MFunction { proc, fn -> RunWith(proc.asProcess, fn.asFunction) }

        @MField("then-run-with")
        @JvmField
        val thenRunWith: MAny = MFunction { proc, fn -> ThenRunWith(proc.asProcess, fn.asFunction) }

        @MField("run-unsafe")
        @JvmField
        val runUnsafe: MAny = MFunction { proc -> proc.asProcess() }

        @MField("create-process")
        @JvmField
        val createProcess: MAny = MFunction { fn -> MProcess { fn.asFunction() } }
    }
}