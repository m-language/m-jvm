package io.github.m

/**
 * M wrapper class for nats.
 */
@ExperimentalUnsignedTypes
data class Nat(val value: UInt) : Value {
    override fun toString() = value.toString()

    /**
     * M nat definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("nat.+")
        @JvmField
        val add: Value = Function { x, y -> Nat((x as Nat).value + (y as Nat).value) }

        @MField("nat.-")
        @JvmField
        val sub: Value = Function { x, y -> Nat((x as Nat).value - (y as Nat).value) }

        @MField("nat.*")
        @JvmField
        val mul: Value = Function { x, y -> Nat((x as Nat).value * (y as Nat).value) }

        @MField("nat./")
        @JvmField
        val div: Value = Function { x, y -> Nat((x as Nat).value / (y as Nat).value) }

        @MField("nat.%")
        @JvmField
        val rem: Value = Function { x, y -> Nat((x as Nat).value % (y as Nat).value) }

        @MField("nat.<")
        @JvmField
        val lt: Value = Function { x, y -> Bool((x as Nat).value < (y as Nat).value) }

        @MField("nat.>")
        @JvmField
        val gt: Value = Function { x, y -> Bool((x as Nat).value > (y as Nat).value) }

        @MField("nat.=")
        @JvmField
        val eq: Value = Function { x, y -> Bool((x as Nat).value == (y as Nat).value) }

        @MField("nat->char")
        @JvmField
        val toChar: Value = Function { x -> Char((x as Nat).value.toInt().toChar()) }
    }
}