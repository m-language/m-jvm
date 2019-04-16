package io.github.m

/**
 * M wrapper class for chars.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
data class Char(val char: kotlin.Char) : Value.Delegate {
    override fun toString() = char.toString()

    override val value get() = Nat(char.toInt().toUInt())

    companion object {
        fun from(value: Value) = value as? Char ?: Char(Nat.from(value).nat.toInt().toChar())
    }

    /**
     * M char definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("char.=")
        @JvmField
        val eq: Value = Value { x, y -> Bool(from(x).char == from(y).char) }

        @MField("char->nat")
        @JvmField
        val toNat: Value = Value { x -> Nat(from(x).char.toInt().toUInt()) }

        @MField("space")
        @JvmField
        val space: Value = Char(' ')

        @MField("tab")
        @JvmField
        val tab: Value = Char('\t')

        @MField("linefeed")
        @JvmField
        val linefeed: Value = Char('\n')

        @MField("carriage-return")
        @JvmField
        val carriageReturn: Value = Char('\r')
    }
}