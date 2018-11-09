package io.github.m

/**
 * Class representing an M environment.
 */
data class Env(val vars: TreeMap,
               val file: List,
               val def: List,
               val index: Int) : Data {
    override val type get() = Companion.type

    override fun get(key: Symbol) = when (key.value) {
        "vars" -> vars
        "file" -> file
        "def" -> def
        "index" -> index
        else -> noField(key)
    }

    companion object : Value {
        override val type = Symbol("env")
    }

    @Suppress("unused")
    object Definitions {
        @MField("env")
        @JvmField
        val env: Value = Function { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            val list3 = list2.cdr.asCons
            val list4 = list3.cdr.asCons
            Env(
                    list.car.cast(),
                    list2.car.cast(),
                    list3.car.cast(),
                    list4.car.cast()
            )
        }
    }
}