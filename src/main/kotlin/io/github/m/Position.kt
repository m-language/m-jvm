package io.github.m

/**
 * Class representing an M position.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
data class Position(val line: UInt, val char: UInt) {
    fun nextChar() = copy(char = char + 1U)
    fun nextLine() = copy(line = line + 1U, char = 1U)
    override fun toString() = "$line:$char"
}