package io.github.m

@Suppress("unused")
object Functions {
    @MField("function->process")
    @JvmField
    val toProcess: Value = Function { fn -> Process { (fn as Function)() } }
}