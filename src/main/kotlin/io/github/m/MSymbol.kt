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
        val eqSymbol: MAny = MFunction { x, y -> x.asSymbol.eq(y.asSymbol) }

        @MField("add-symbol")
        @JvmField
        val addSymbol: MAny = MFunction { x, y -> x.asSymbol.add(y.asSymbol) }

        @MField("symbol->char")
        @JvmField
        val symbolToChar: MAny = MFunction { x -> MChar(x.asSymbol.value.first()) }

        @MField("symbol->int")
        @JvmField
        val symbolToInt: MAny = MFunction { x -> MInt(x.asSymbol.value.toInt()) }

        @MField("symbol->list")
        @JvmField
        val symbolToList: MAny = MFunction { arg -> arg.asSymbol.value.mString }

        @MField("symbol->real")
        @JvmField
        val symbolToReal: MAny = MFunction { x -> MReal(x.asSymbol.value.toFloat()) }
    }
}