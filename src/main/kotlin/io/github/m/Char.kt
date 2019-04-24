package io.github.m

/**
 * M wrapper class for chars.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
data class Char(val char: kotlin.Char) : Value.Delegate {
    override fun toString() = char.toString()

    override fun value() = Nat(char.toInt())

    companion object {
        fun from(value: Value) = value as? Char ?: Char(Nat.from(value).value.toChar())
    }

    /**
     * M char definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField(name = "char.=")
        @JvmField
        val eq: Value = Value.Impl2 { x, y -> Bool(from(x).char == from(y).char) }

        @MField(name = "char->nat")
        @JvmField
        val toNat: Value = Value.Impl1 { x -> Nat(from(x).char.toInt()) }

        @MField(name = "space")
        @JvmField
        val space: Value = Char(' ')

        @MField(name = "tab")
        @JvmField
        val tab: Value = Char('\t')

        @MField(name = "linefeed")
        @JvmField
        val linefeed: Value = Char('\n')

        @MField(name = "carriage-return")
        @JvmField
        val carriageReturn: Value = Char('\r')
    }
}