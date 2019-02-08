package io.github.m

/**
 * Class representing an M variable.
 */
@ExperimentalUnsignedTypes
sealed class Variable {
    data class Local(val name: List, val index: Nat) : Variable()
    data class Global(val name: List, val path: List) : Variable()
}