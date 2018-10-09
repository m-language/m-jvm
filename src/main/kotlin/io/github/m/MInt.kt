package io.github.m

/**
 * M wrapper class for integers.
 */
data class MInt(val value: Int) : MAny {
    override val type get() = Companion.type

    fun add(i: MInt) = MInt(value + i.value)
    fun sub(i: MInt) = MInt(value - i.value)
    fun mul(i: MInt) = MInt(value * i.value)
    fun div(i: MInt) = MInt(value / i.value)
    fun rem(i: MInt) = MInt(value % i.value)
    fun lt(i: MInt) = MBool.valueOf(value < i.value)
    fun gt(i: MInt) = MBool.valueOf(value > i.value)
    fun eq(i: MInt) = MBool.valueOf(value == i.value)

    override fun toString() = value.toString()

    companion object : MAny {
        /**
         * The type of all integers.
         */
        override val type = MSymbol("int")

        @JvmStatic
        fun valueOf(value: Int) = MInt(value)
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("add-int")
        @JvmField
        val addInt: MAny = MFunction { x, y -> Cast.toInt(x).add(Cast.toInt(y)) }

        @MField("sub-int")
        @JvmField
        val subInt: MAny = MFunction { x, y -> Cast.toInt(x).sub(Cast.toInt(y)) }

        @MField("mul-int")
        @JvmField
        val mulInt: MAny = MFunction { x, y -> Cast.toInt(x).mul(Cast.toInt(y)) }

        @MField("div-int")
        @JvmField
        val divInt: MAny = MFunction { x, y -> Cast.toInt(x).div(Cast.toInt(y)) }

        @MField("rem-int")
        @JvmField
        val remInt: MAny = MFunction { x, y -> Cast.toInt(x).rem(Cast.toInt(y)) }

        @MField("lt-int")
        @JvmField
        val ltInt: MAny = MFunction { x, y -> Cast.toInt(x).lt(Cast.toInt(y)) }

        @MField("gt-int")
        @JvmField
        val gtInt: MAny = MFunction { x, y -> Cast.toInt(x).gt(Cast.toInt(y)) }

        @MField("eq-int")
        @JvmField
        val eqInt: MAny = MFunction { x, y -> Cast.toInt(x).eq(Cast.toInt(y)) }

        @MField("symbol->int")
        @JvmField
        val symbolToInt: MAny = MFunction { x -> MInt(Cast.toSymbol(x).value.toInt()) }
    }
}