package io.github.m

/**
 * M wrapper class for strings.
 */
data class Symbol(val value: String) : Value {
    override val type get() = Companion.type

    fun eq(symbol: Symbol) = Bool.valueOf(value == symbol.value)

    fun add(symbol: Symbol) = Symbol(value + symbol.value)

    override fun toString() = value

    companion object : Value {
        /**
         * The type of all symbols.
         */
        override val type = Symbol("symbol")
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("eq-symbol")
        @JvmField
        val eqSymbol: Value = Function { x, y -> x.asSymbol.eq(y.asSymbol) }

        @MField("add-symbol")
        @JvmField
        val addSymbol: Value = Function { x, y -> x.asSymbol.add(y.asSymbol) }

        @MField("symbol->char")
        @JvmField
        val symbolToChar: Value = Function { x -> Char(x.asSymbol.value.first()) }

        @MField("symbol->int")
        @JvmField
        val symbolToInt: Value = Function { x -> Int(x.asSymbol.value.toInt()) }

        @MField("symbol->list")
        @JvmField
        val symbolToList: Value = Function { arg -> arg.asSymbol.value.m }

        @MField("symbol->real")
        @JvmField
        val symbolToReal: Value = Function { x -> Real(x.asSymbol.value.toFloat()) }
    }
}