package io.github.m

data class MPair(val first: MAny, val second: MAny) : MData {
    override val type get() = Companion.type

    override fun get(key: MSymbol) = when (key.value) {
        "first" -> first
        "second" -> second
        else -> noField(key)
    }

    companion object : MAny {
        override val type = MSymbol("pair")
    }

    @Suppress("unused")
    object Definitions {
        @MField("pair")
        @JvmField
        val pair: MAny = MFunction { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            MPair(list.car.cast(), list2.car.cast())
        }
    }
}
