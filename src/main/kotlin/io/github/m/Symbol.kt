package io.github.m

/**
 * M wrapper class for strings.
 */
data class Symbol(val string: String) : Value.Delegate {
    override fun toString() = string

    override fun value() = when {
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
        val eq: Value = Value.Impl2 { x, y -> Bool(from(x).string == from(y).string) }

        @MField("symbol.+")
        @JvmField
        val add: Value = Value.Impl2 { x, y -> Symbol(from(x).string + from(y).string) }

        @MField("symbol->list")
        @JvmField
        val toList: Value = Value.Impl1 { x -> from(x).string.toList }

        @MField("normalize")
        @JvmField
        val normalize: Value = Value.Impl1 { x -> Symbol(Symbol.from(x).string.normalize()) }

        @MField("unnormalize")
        @JvmField
        val unnormalize: Value = Value.Impl1 { x -> Symbol(Symbol.from(x).string.unnormalize()) }
    }
}