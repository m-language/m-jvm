package io.github.m

/**
 * Class representing M expressions.
 */
sealed class Expr : MAny {
    /**
     * The line the expression is on.
     */
    abstract val line: Int

    data class Identifier(val name: String, override val line: Int) : Expr(), MData {
        constructor(name: Sequence<Char>, line: Int) : this(name.joinToString("", "", ""), line)

        override fun get(key: MSymbol) = when (key.value) {
            "name" -> name.mString
            "line" -> MInt(line)
            else -> noField(key)
        }

        override val type get() = Companion.type

        override fun toString() = name

        companion object : MAny {
            override val type = MSymbol("identifier-expr")
        }
    }

    data class List(val exprs: kotlin.collections.List<Expr>, override val line: Int) : Expr(), MData {
        override fun get(key: MSymbol) = when (key.value) {
            "exprs" -> MList.valueOf(exprs)
            "line" -> MInt(line)
            else -> noField(key)
        }

        override val type get() = Companion.type

        override fun toString() = exprs.joinToString(" ", "(", ")")

        companion object : MAny {
            override val type= MSymbol("list-expr")
        }
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("identifier-expr")
        @JvmField
        val identifierExpr: MAny = MFunction { fields ->
            val list = Cast.toList(fields)
            Identifier(Cast.toList(list.car).string, Cast.toInt(list.cdr.car).value)
        }

        @MField("list-expr")
        @JvmField
        val listExpr: MAny = MFunction { fields ->
            val list = Cast.toList(fields)
            List(Cast.toList(list.car).asSequence().map { it as Expr }.toList(), Cast.toInt(list.cdr.car).value)
        }
    }
}