package io.github.m

/**
 * M implementation of IO processes.
 */
@FunctionalInterface
interface Process : Function {
    fun run(): Value

    override fun invoke(arg: Value) = ThenRunWith(this, arg as Function)

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: () -> Value) = Impl(fn)
    }

    class Do(val value: Value) : Process {
        override fun run(): Value = value
    }

    class Impl(val fn: () -> Value) : Process {
        override fun run(): Value = fn()
    }

    class ThenRun(val a: Process, val b: Process) : Process {
        override fun run(): Value = runAll(this)

        private tailrec fun runAll(process: Process): Value = when (process) {
            is ThenRun -> {
                process.a()
                runAll(process.b)
            }
            else -> process()
        }
    }

    class RunWith(val process: Process, val function: Function) : Process {
        override fun run(): Value = function(process.run())
    }

    class ThenRunWith(val process: Process, val function: Function) : Process {
        override fun run(): Value = (function(process.run()) as Process).run()

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
        @MField("return")
        @JvmField
        val `return`: Value = Function { value -> Process.Do(value) }

        @MField("then-run-with")
        @JvmField
        val thenRunWith: Value = Function { proc, fn -> Process.ThenRunWith(proc as Process, fn as Function) }

        @MField("then-run")
        @JvmField
        val thenRun: Value = Function { proc1, proc2 -> Process.ThenRun(proc1 as Process, proc2 as Process) }

        @MField("run-with")
        @JvmField
        val runWith: Value = Function { proc, fn -> Process.RunWith(proc as Process, fn as Function) }
    }
}