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
        val pair: Value = Function { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            Pair(list.car.cast(), list2.car.cast())
        }
    }
}
