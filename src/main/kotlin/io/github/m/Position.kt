package io.github.m

/**
 * Class representing an M position.
 */
data class Position(val line: Int, val char: Int) {
    fun nextChar() = copy(char = char + 1)
    fun nextLine() = copy(line = line + 1, char = 1)
    override fun toString() = "$line:$char"
}