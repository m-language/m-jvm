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
        override val type = MKeyword("char")

        @JvmStatic
        fun valueOf(c: Char) = MChar(c)
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("c")
        @JvmField
        val c: MAny = MFunction { x -> valueOf(Cast.toKeyword(x).value.toInt().toChar()) }

        @MField("eq-char")
        @JvmField
        val eqChar: MAny = MFunction { x, y -> Cast.toChar(x).eq(Cast.toChar(y)) }
    }
}