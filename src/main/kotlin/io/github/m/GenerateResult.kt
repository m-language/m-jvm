package io.github.m

import io.github.m.asm.Declaration
import io.github.m.asm.Operation

/**
 * Class representing an M generate result.
 */
data class GenerateResult(val operation: Operation, val declaration: Declaration, val env: Env)