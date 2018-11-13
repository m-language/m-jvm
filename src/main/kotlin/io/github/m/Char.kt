package io.github.m

/**
 * M wrapper class for characters.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
data class Char(val value: kotlin.Char) : Value {
    override val type get() = Companion.type

    fun eq(c: Char) = Bool.valueOf(value == c.value)

    override fun toString() = value.toString()

    companion object : Value {
        /**
         * The type of all characters.
         */
        override val type = Symbol("char")

        @JvmStatic
        fun valueOf(c: kotlin.Char) = Char(c)
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("eq-char")
        @JvmField
        val eqChar: Value = Function { x, y -> x.asChar.eq(y.asChar) }

        @MField("char->nat")
        @JvmField
        val charToNat: Value = Function { x -> Nat(x.asChar.value.toInt().toUInt()) }
    }
}