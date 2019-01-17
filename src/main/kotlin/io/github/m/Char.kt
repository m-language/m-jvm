package io.github.m

/**
 * M wrapper class for chars.
 */
data class Char(val value: kotlin.Char) : List {
    override fun toString() = value.toString()

    override fun invoke(arg: Value) = when (value.toInt()) {
        0 -> List.nil(arg)
        else -> List.cons(List.nil, Char(value - 1))(arg)
    }

    @ExperimentalUnsignedTypes
    override fun iterator() = Nat(value.toInt().toUInt()).iterator()

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
        val eq: Value = Value { x, y -> Bool(Char.from(x).value == Char.from(y).value) }

        @MField("char->nat")
        @JvmField
        @ExperimentalUnsignedTypes
        val toNat: Value = Value { x -> Nat(Char.from(x).value.toInt().toUInt()) }
    }
}