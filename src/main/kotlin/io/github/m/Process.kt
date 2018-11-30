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
}