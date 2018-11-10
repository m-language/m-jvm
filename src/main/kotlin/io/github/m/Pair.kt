package io.github.m

/**
 * Class representing an M pair.
 */
data class Pair(val first: Value, val second: Value) : Data {
    override val type get() = Companion.type

    override fun get(key: Symbol) = when (key.value) {
        "first" -> first
        "second" -> second
        else -> noField(key)
    }

    companion object : Value {
        override val type = Symbol("pair")
    }

    @Suppress("unused")
    object Definitions {
        @MField("pair")
        @JvmField
        val pair: Value = Function { first, second ->
            Pair(first, second)
        }
    }
}
