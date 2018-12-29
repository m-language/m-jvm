package io.github.m

/**
 * M wrapper class for chars.
 */
data class Char(val value: kotlin.Char) : Value {
    override fun toString() = value.toString()

    /**
     * M char definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("char.=")
        @JvmField
        val eq: Value = Function { x, y -> Bool((x as Char).value == (y as Char).value) }

        @MField("char->nat")
        @JvmField
        @ExperimentalUnsignedTypes
        val toNat: Value = Function { x -> Nat((x as Char).value.toInt().toUInt()) }
    }
}