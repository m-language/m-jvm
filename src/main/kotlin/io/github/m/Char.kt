package io.github.m

/**
 * M wrapper class for chars.
 */
data class Char(val value: kotlin.Char) : Value {
    override fun toString() = value.toString()

    @ExperimentalUnsignedTypes
    override fun invoke(arg: Value) = Nat(value.toInt().toUInt())(arg)

    companion object {
        fun from(value: Value) = value as? Char ?: Char(List.from(value).count().toChar())
    }

    /**
     * M char definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("char.=")
        @JvmField
        val eq: Value = Value { x, y -> Bool(from(x).value == from(y).value) }

        @MField("char->nat")
        @JvmField
        @ExperimentalUnsignedTypes
        val toNat: Value = Value { x -> Nat(from(x).value.toInt().toUInt()) }
    }
}