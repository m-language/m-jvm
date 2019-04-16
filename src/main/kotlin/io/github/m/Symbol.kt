package io.github.m

/**
 * M wrapper class for strings.
 */
data class Symbol(val string: String) : Value.Delegate {
    override fun toString() = string

    override val value get() = when {
        string.isEmpty() -> List.Nil
        else -> Pair(Char(string[0]), Symbol(string.drop(1)))
    }

    companion object {
        fun from(value: Value) = value as? Symbol ?: Symbol(String(List.from(value).map { Char.from(it).char }.toCharArray()))
    }

    /**
     * M symbol definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("symbol.=")
        @JvmField
        val eq: Value = Value { x, y -> Bool(from(x).string == from(y).string) }

        @MField("symbol.+")
        @JvmField
        val add: Value = Value { x, y -> Symbol(from(x).string + from(y).string) }

        @MField("symbol->list")
        @JvmField
        val toList: Value = Value { x -> from(x).string.toList }

        @MField("normalize")
        @JvmField
        val normalize: Value = Value { x -> Symbol(Symbol.from(x).string.normalize()) }

        @MField("unnormalize")
        @JvmField
        val unnormalize: Value = Value { x -> Symbol(Symbol.from(x).string.unnormalize()) }
    }
}