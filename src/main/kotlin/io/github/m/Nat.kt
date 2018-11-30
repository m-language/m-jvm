package io.github.m

/**
 * M wrapper class for nats.
 */
@ExperimentalUnsignedTypes
data class Nat(val value: UInt) : Value {
    override fun toString() = value.toString()
}