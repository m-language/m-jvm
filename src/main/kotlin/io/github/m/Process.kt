package io.github.m

/**
 * M implementation of IO processes.
 */
@FunctionalInterface
interface Process : Value {
    @JvmDefault
    override val type
        get() = Companion.type

    operator fun invoke(): Value

    companion object : Value {
        /**
         * The type of all processes.
         */
        override val type = Symbol("process")

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: () -> Value) = Impl(fn)
    }

    class Impl(val fn: () -> Value) : Process {
        override fun invoke(): Value = fn()
    }

    /**
     * Implementation of [Definitions.thenRun].
     */
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

    /**
     * Implementation of [Definitions.runWith].
     */
    class RunWith(val process: Process, val function: Function) : Process {
        override fun invoke(): Value = function(process())
    }

    /**
     * Implementation of [Definitions.thenRunWith].
     */
    class ThenRunWith(val process: Process, val function: Function) : Process {
        override fun invoke(): Value = function(process()).asProcess()

//        private tailrec fun rec(process: Process): Value = when (process) {
//            is ThenRunWith -> rec(function(process.process()).asProcess)
//            else -> process()
//        }
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("then-run")
        @JvmField
        val thenRun: Value = Function { proc1, proc2 -> ThenRun(proc1.asProcess, proc2.asProcess) }

        @MField("run-with")
        @JvmField
        val runWith: Value = Function { proc, fn -> RunWith(proc.asProcess, fn.asFunction) }

        @MField("then-run-with")
        @JvmField
        val thenRunWith: Value = Function { proc, fn -> ThenRunWith(proc.asProcess, fn.asFunction) }

        @MField("run-unsafe")
        @JvmField
        val runUnsafe: Value = Function { proc -> proc.asProcess() }

        @MField("function->process")
        @JvmField
        val createProcess: Value = Function { fn -> Process { fn.asFunction() } }
    }
}