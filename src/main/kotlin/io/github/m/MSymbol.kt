package io.github.m

/**
 * M wrapper class for strings.
 */
data class MSymbol(val value: String) : MAny {
    override val type get() = Companion.type

    fun eq(k: MSymbol) = MBool.valueOf(value == k.value)

    fun add(k: MSymbol) = MSymbol(value + k.value)

    override fun toString() = value

    companion object : MAny {
        /**
         * The type of all symbols.
         */
        override val type = MSymbol("symbol")
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("eq-symbol")
        @JvmField
        val eqSymbol: MAny = MFunction { x, y -> Cast.toSymbol(x).eq(Cast.toSymbol(y)) }

        @MField("add-symbol")
        @JvmField
        val addSymbol: MAny = MFunction { x, y -> Cast.toSymbol(x).add(Cast.toSymbol(y)) }
    }
}