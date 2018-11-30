package io.github.m

/**
 * M wrapper class for files
 */
data class File(val value: java.io.File) : Value {
    override fun toString() = value.toString()
}