package io.github.m

/**
 * M wrapper class for strings.
 */
data class Symbol(val value: String) : Value {
    override fun toString() = value

    /**
     * M symbol definitions.
     */
    @Suppress("unused")
    @ExperimentalUnsignedTypes
    object Definitions {
        @MField("symbol.=")
        @JvmField
        val eq: Value = Function { x, y -> Bool((x as Symbol).value == (y as Symbol).value) }

        @MField("symbol.+")
        @JvmField
        val add: Value = Function { x, y -> Symbol((x as Symbol).value + (y as Symbol).value) }

        @MField("symbol->char")
        @JvmField
        val toChar: Value = Function { x -> Char((x as Symbol).value.first()) }

        @MField("symbol->nat")
        @JvmField
        val toNat: Value = Function { x -> Nat((x as Symbol).value.toUInt()) }

        @MField("symbol->list")
        @JvmField
        val toList: Value = Function { x -> (x as Symbol).value.toList }

        @MField("symbol->real")
        @JvmField
        val toReal: Value = Function { x -> Real((x as Symbol).value.toFloat()) }
    }
}