package io.github.m

/**
 * Class representing an M maybe.
 */
sealed class Maybe : MData {
    object None : Maybe() {
        override val type = MSymbol("none")
        override fun get(key: MSymbol) = noField(key)
    }

    data class Some(val value: MAny) : Maybe() {
        override val type get() = Companion.type

        override fun get(key: MSymbol) = when (key.value) {
            "value" -> value
            else -> noField(key)
        }

        companion object : MAny {
            override val type = MSymbol("some")
        }
    }

    @Suppress("unused")
    object Definitions {
        @MField("some")
        @JvmField
        val some: MAny = MFunction { fields ->
            val list = fields.asCons
            Some(list.car)
        }
    }
}
