package io.github.m

import io.github.m.asm.MType

/**
 * Class representing an M environment
 */
data class Env(val vars: TreeMap,
               val file: MType,
               val def: MString,
               val index: MInt) : MData {
    override val type get() = Companion.type

    override fun get(key: MSymbol) = when (key.value) {
        "vars" -> vars
        "file" -> file
        "def" -> def
        "index" -> index
        else -> noField(key)
    }

    companion object : MAny {
        override val type = MSymbol("env")
    }

    @Suppress("unused")
    object Definitions {
        @MField("env")
        @JvmField
        val env: MAny = MFunction { fields ->
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