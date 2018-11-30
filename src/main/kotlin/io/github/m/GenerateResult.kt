package io.github.m

/**
 * Class representing an M generate result.
 */
@ExperimentalUnsignedTypes
data class GenerateResult(val operation: Operation, val declaration: Declaration, val env: Env)