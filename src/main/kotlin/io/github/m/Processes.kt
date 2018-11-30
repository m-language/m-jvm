package io.github.m

/**
 * M process definitions.
 */
@Suppress("unused")
object Processes {
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