package io.github.m

/**
 * M wrapper class for strings.
 */
data class Symbol(val value: String) : Value {
    override fun toString() = value

    override fun invoke(arg: Value) =
            if (value.isEmpty()) List.Nil(arg)
            else Pair.Impl(Char(value[0]), Symbol(value.drop(1)))(arg)

    companion object {
        fun from(value: Value) = value as? Symbol ?: Symbol(String(List.from(value).map { Char.from(it).value }.toCharArray()))
    }

    /**
     * M symbol definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("symbol.=")
        @JvmField
        val eq: Value = Value { x, y -> Bool(from(x).value == from(y).value) }

        @MField("symbol.+")
        @JvmField
        val add: Value = Value { x, y -> Symbol(from(x).value + from(y).value) }

        @MField("symbol->list")
        @JvmField
        val toList: Value = Value { x -> from(x).value.toList }

        @MField("normalize")
        @JvmField
        val normalize: Value = Value { x -> Symbol(Symbol.from(x).value.normalize()) }

        @MField("unnormalize")
        @JvmField
        val unnormalize: Value = Value { x -> Symbol(Symbol.from(x).value.unnormalize()) }
    }
}