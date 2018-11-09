package io.github.m

/**
 * M wrapper class for characters.
 */
data class MChar(val value: Char) : MAny {
    override val type get() = Companion.type

    fun eq(c: MChar) = MBool.valueOf(value == c.value)

    override fun toString() = value.toString()

    @Suppress("unused")
    companion object : MAny {
        /**
         * The type of all characters.
         */
        override val type = MSymbol("char")

        @JvmStatic
        fun valueOf(c: Char) = MChar(c)
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("eq-char")
        @JvmField
        val eqChar: MAny = MFunction { x, y -> x.asChar.eq(y.asChar) }

        @MField("char->int")
        @JvmField
        val charToInt: MAny = MFunction { x -> MInt(x.asChar.value.toInt()) }
    }
}