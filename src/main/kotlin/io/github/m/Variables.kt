package io.github.m

/**
 * M variable definitions.
 */
@Suppress("unused")
@ExperimentalUnsignedTypes
object Variables {
    @MField("local-variable")
    @JvmField
    val local: Value = Function { name, index -> Variable.Local(name as List, index as Nat) }

    @MField("global-variable")
    @JvmField
    val global: Value = Function { name, file -> Variable.Global(name as List, file as List) }
}