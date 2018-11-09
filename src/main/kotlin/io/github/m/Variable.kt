package io.github.m

/**
 * Class representing an M variable.
 */
sealed class Variable : Value {
    data class Local(val name: List, val index: Int) : Variable(), Data {
        override val type get() = Companion.type

        override fun get(key: Symbol) = when (key.value) {
            "name" -> name
            "index" -> index
            else -> noField(key)
        }

        companion object : Value {
            override val type get() = Symbol("local-variable")
        }
    }

    data class Global(val name: List, val file: List) : Variable(), Data {
        override val type get() = Companion.type

        override fun get(key: Symbol) = when (key.value) {
            "name" -> name
            "file" -> file
            else -> noField(key)
        }

        companion object : Value {
            override val type = Symbol("global-variable")
        }
    }

    companion object : Value {
        override val type = Symbol("variable")
    }

    @Suppress("unused")
    object Definitions {
        @MField("global-variable")
        @JvmField
        val globalVariable: Value = Function { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            Global(list.car.cast(), list2.car.cast())
        }

        @MField("local-variable")
        @JvmField
        val localVariable: Value = Function { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            Local(list.car.cast(), list2.car.cast())
        }
    }
}