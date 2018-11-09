package io.github.m

/**
 * Class representing an M maybe.
 */
sealed class Maybe : Data {
    object None : Maybe() {
        override val type = Symbol("none")
        override fun get(key: Symbol) = noField(key)
    }

    data class Some(val value: Value) : Maybe() {
        override val type get() = Companion.type

        override fun get(key: Symbol) = when (key.value) {
            "value" -> value
            else -> noField(key)
        }

        companion object : Value {
            override val type = Symbol("some")
        }
    }

    @Suppress("unused")
    object Definitions {
        @MField("some")
        @JvmField
        val some: Value = Function { fields ->
            val list = fields.asCons
            Some(list.car)
        }
    }
}
