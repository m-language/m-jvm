package io.github.m

/**
 * M wrapper class for strings.
 */
data class Symbol(val value: String) : List {
    override fun toString() = value

    override fun invoke(arg: Value) =
            if (value.isEmpty())
                List.nil(arg)
            else
                List.cons(Char(value[0]), Symbol(value.drop(1)))(arg)

    override fun iterator() = value.asSequence().map(::Char).iterator()

    companion object {
        fun from(value: Value) = value as? Symbol ?: Symbol(List.from(value).joinToString("", "", ""))
    }

    /**
     * M symbol definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("symbol.=")
        @JvmField
        val eq: Value = Value { x, y -> Bool(Symbol.from(x).value == Symbol.from(y).value) }

        @MField("symbol.+")
        @JvmField
        val add: Value = Value { x, y -> Symbol(Symbol.from(x).value + Symbol.from(y).value) }

        @MField("symbol->list")
        @JvmField
        val toList: Value = Value { x -> Symbol.from(x).value.toList }
    }
}