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
        @MField("local-variable")
        @JvmField
        val localVariable: Value = Function { name, index ->
            Local(name.asList, index.asInt)
        }

        @MField("global-variable")
        @JvmField
        val globalVariable: Value = Function { name, file ->
            Global(name.asList, file.asList)
        }
    }
}