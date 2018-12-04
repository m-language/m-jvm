package io.github.m

/**
 * M implementation of IO processes.
 */
@FunctionalInterface
interface Process : Value {
    operator fun invoke(): Value

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: () -> Value) = Impl(fn)
    }

    class Impl(val fn: () -> Value) : Process {
        override fun invoke(): Value = fn()
    }

    class ThenRun(val a: Process, val b: Process) : Process {
        override fun invoke(): Value = runAll(this)

        private tailrec fun runAll(process: Process): Value = when (process) {
            is ThenRun -> {
                process.a()
                runAll(process.b)
            }
            else -> process()
        }
    }

    class RunWith(val process: Process, val function: Function) : Process {
        override fun invoke(): Value = function(process())
    }

    class ThenRunWith(val process: Process, val function: Function) : Process {
        override fun invoke(): Value = (function(process()) as Process)()

//        private tailrec fun rec(process: Process): Value = when (process) {
//            is ThenRunWith -> rec(function(process.process()).asProcess)
//            else -> process()
//        }
    }

    /**
     * M process definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("then-run")
        @JvmField
        val thenRun: Value = Function { proc1, proc2 -> Process.ThenRun(proc1 as Process, proc2 as Process) }

        @MField("run-with")
        @JvmField
        val runWith: Value = Function { proc, fn -> Process.RunWith(proc as Process, fn as Function) }

        @MField("then-run-with")
        @JvmField
        val thenRunWith: Value = Function { proc, fn -> Process.ThenRunWith(proc as Process, fn as Function) }

        @MField("run-unsafe")
        @JvmField
        val runUnsafe: Value = Function { proc -> (proc as Process)() }
    }
}