package io.github.m

/**
 * Class representing an M expression.
 */
sealed class Expr : MAny {
    abstract val line: MInt

    data class Identifier(val name: MList, override val line: MInt) : Expr(), MData {
        constructor(name: Sequence<Char>, line: Int) : this(MList.valueOf(name.map { MChar(it) }), MInt(line))

        override val type get() = Companion.type

        override fun get(key: MSymbol) = when (key.value) {
            "name" -> name
            "line" -> line
            else -> noField(key)
        }

        override fun toString() = name.asString

        companion object : MAny {
            override val type = MSymbol("identifier-expr")
        }
    }

    data class List(val exprs: MList, override val line: MInt) : Expr(), MData {
        constructor(exprs: Sequence<Expr>, line: Int) : this(MList.valueOf(exprs), MInt(line))

        override val type get() = Companion.type

        override fun get(key: MSymbol) = when (key.value) {
            "exprs" -> MList.valueOf(exprs)
            "line" -> line
            else -> noField(key)
        }

        override fun toString() = exprs.joinToString(" ", "(", ")")

        companion object : MAny {
            override val type= MSymbol("list-expr")
        }
    }

    @Suppress("unused")
    object Definitions {
        @MField("identifier-expr")
        @JvmField
        val identifierExpr: MAny = MFunction { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            Identifier(list.car.asList, list2.car.asInt)
        }

        @MField("list-expr")
        @JvmField
        val listExpr: MAny = MFunction { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            List(list.car.asList, list2.car.asInt)
        }
    }
}