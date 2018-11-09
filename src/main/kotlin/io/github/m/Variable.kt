package io.github.m

/**
 * Class representing the location of an M variable.
 */
sealed class Variable : MAny {
    /**
     * A local variable introduced by a lambda expression.
     *
     * @param name  The name of the parameter.
     * @param index The index of the parameter.
     */
    data class Local(val name: MString, val index: MInt) : Variable(), MData {
        override val type get() = Companion.type

        override fun get(key: MSymbol) = when (key.value) {
            "name" -> name
            "index" -> index
            else -> noField(key)
        }

        companion object : MAny {
            override val type get() = MSymbol("local-variable")
        }
    }

    /**
     * A global variable introduced by a def expression.
     *
     * @param name The name of the definition.
     * @param file The file of the definition.
     */
    data class Global(val name: MString, val file: MList) : Variable(), MData {
        override val type get() = Companion.type

        override fun get(key: MSymbol) = when (key.value) {
            "name" -> name
            "file" -> file
            else -> noField(key)
        }

        companion object : MAny {
            override val type = MSymbol("global-variable")
        }
    }

    companion object : MAny {
        override val type = MSymbol("variable")
    }

    @Suppress("unused")
    object Definitions {
        @MField("global-variable")
        @JvmField
        val globalVariable: MAny = MFunction { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            Global(list.car.cast(), list2.car.cast())
        }

        @MField("local-variable")
        @JvmField
        val localVariable: MAny = MFunction { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            Local(list.car.cast(), list2.car.cast())
        }
    }
}