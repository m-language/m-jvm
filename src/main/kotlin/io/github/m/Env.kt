package io.github.m

/**
 * Class representing an M environment.
 */
@ExperimentalUnsignedTypes
data class Env(val vars: Map<String, Variable>,
               val path: String,
               val def: String,
               val index: UInt)