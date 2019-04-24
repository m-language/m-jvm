package io.github.m

/**
 * M implementation of IO processes.
 */
@FunctionalInterface
interface Process : Value {
    fun run(): Value

    override fun invoke(arg: Value) = ThenRunWith(this, arg)

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: () -> Value) = Impl(fn)
    }

    class Impure(val value: Value) : Process {
        override fun run(): Value = value
    }

    class Impl(val fn: () -> Value) : Process {
        override fun run(): Value = fn()
    }

    class ThenRun(val a: Process, val b: Process) : Process {
        override fun run(): Value = run { a.run(); b.run() }
    }

    class RunWith(val process: Process, val function: Value) : Process {
        override fun run(): Value = function(process.run())
    }

    class ThenRunWith(val process: Process, val function: Value) : Process {
        override fun run(): Value = (function(process.run()) as Process).run()
    }

    /**
     * M process definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField(name = "impure")
        @JvmField
        val impure: Value = Value.Impl1 { value -> Impure(value) }

        @MField(name = "then-run-with")
        @JvmField
        val thenRunWith: Value = Value.Impl2 { proc, fn -> ThenRunWith(proc as Process, fn) }

        @MField(name = "then-run")
        @JvmField
        val thenRun: Value = Value.Impl2 { proc1, proc2 -> ThenRun(proc1 as Process, proc2 as Process) }

        @MField(name = "run-with")
        @JvmField
        val runWith: Value = Value.Impl2 { proc, fn -> RunWith(proc as Process, fn) }
    }
}