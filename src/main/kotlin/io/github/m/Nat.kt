package io.github.m

/**
 * M wrapper class for nats.
 */
@ExperimentalUnsignedTypes
data class Nat(val value: UInt) : Value {
    override fun toString() = value.toString()

    override fun invoke(arg: Value) = when (value) {
        0U -> Either.Left(Value { x -> x })
        else -> Either.Right(Nat(value - 1U))
    }(arg)

    companion object {
        fun from(value: Value) = value as? Nat ?: Either.from(value).run {
            fun nat(either: Either): UInt = when (either) {
                is Either.Left -> 0U
                is Either.Right -> nat(Either.from(either.value)) + 1U
            }

            Nat(nat(this))
        }
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
        val isZero: Value = Value { x -> Bool(from(x).value == 0U) }

        @MField("nat.inc")
        @JvmField
        val inc: Value = Value { x -> Nat(from(x).value + 1U) }

        @MField("nat.dec")
        @JvmField
        val dec: Value = Value { x -> Nat(from(x).value - 1U) }

        @MField("nat.+")
        @JvmField
        val add: Value = Value { x, y -> Nat(from(x).value + from(y).value) }

        @MField("nat.-")
        @JvmField
        val sub: Value = Value { x, y -> Nat(from(x).value - from(y).value) }

        @MField("nat.*")
        @JvmField
        val mul: Value = Value { x, y -> Nat(from(x).value * from(y).value) }

        @MField("nat./")
        @JvmField
        val div: Value = Value { x, y -> Nat(from(x).value / from(y).value) }

        @MField("nat.%")
        @JvmField
        val rem: Value = Value { x, y -> Nat(from(x).value % from(y).value) }

        @MField("nat.<")
        @JvmField
        val lt: Value = Value { x, y -> Bool(from(x).value < from(y).value) }

        @MField("nat.>")
        @JvmField
        val gt: Value = Value { x, y -> Bool(from(x).value > from(y).value) }

        @MField("nat.=")
        @JvmField
        val eq: Value = Value { x, y -> Bool(from(x).value == from(y).value) }

        @MField("nat->char")
        @JvmField
        val toChar: Value = Value { x -> Char(from(x).value.toInt().toChar()) }
    }
}