package io.github.m

/**
 * M wrapper class for nats.
 */
@ExperimentalUnsignedTypes
data class Nat(val value: UInt) : List {
    override fun toString() = value.toString()

    override fun invoke(arg: Value) = when (value) {
        0U -> List.nil(arg)
        else -> List.cons(List.nil, Nat(value - 1U))(arg)
    }

    override fun iterator() = (0U until value).map { List.nil }.iterator()

    companion object {
        fun from(value: Value) = value as? Nat ?: Nat(List.from(value).count().toUInt())
    }

    /**
     * M nat definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("nat.0")
        @JvmField
        val zero: Value = Nat(0U)

        @MField("nat.1")
        @JvmField
        val one: Value = Nat(1U)

        @MField("nat.0?")
        @JvmField
        val isZero: Value = Value { x -> Bool(Nat.from(x).value == 0U) }

        @MField("nat.inc")
        @JvmField
        val inc: Value = Value { x -> Nat(Nat.from(x).value + 1U) }

        @MField("nat.dec")
        @JvmField
        val dec: Value = Value { x -> Nat(Nat.from(x).value - 1U) }

        @MField("nat.+")
        @JvmField
        val add: Value = Value { x, y -> Nat(Nat.from(x).value + Nat.from(y).value) }

        @MField("nat.-")
        @JvmField
        val sub: Value = Value { x, y -> Nat(Nat.from(x).value - Nat.from(y).value) }

        @MField("nat.*")
        @JvmField
        val mul: Value = Value { x, y -> Nat(Nat.from(x).value * Nat.from(y).value) }

        @MField("nat./")
        @JvmField
        val div: Value = Value { x, y -> Nat(Nat.from(x).value / Nat.from(y).value) }

        @MField("nat.%")
        @JvmField
        val rem: Value = Value { x, y -> Nat(Nat.from(x).value % Nat.from(y).value) }

        @MField("nat.<")
        @JvmField
        val lt: Value = Value { x, y -> Bool(Nat.from(x).value < Nat.from(y).value) }

        @MField("nat.>")
        @JvmField
        val gt: Value = Value { x, y -> Bool(Nat.from(x).value > Nat.from(y).value) }

        @MField("nat.=")
        @JvmField
        val eq: Value = Value { x, y -> Bool(Nat.from(x).value == Nat.from(y).value) }

        @MField("nat->char")
        @JvmField
        val toChar: Value = Value { x -> Char(Nat.from(x).value.toInt().toChar()) }
    }
}