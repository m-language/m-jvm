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
        val addInt: MAny = MFunction { x, y -> x.asInt.add(y.asInt) }

        @MField("sub-int")
        @JvmField
        val subInt: MAny = MFunction { x, y -> x.asInt.sub(y.asInt) }

        @MField("mul-int")
        @JvmField
        val mulInt: MAny = MFunction { x, y -> x.asInt.mul(y.asInt) }

        @MField("div-int")
        @JvmField
        val divInt: MAny = MFunction { x, y -> x.asInt.div(y.asInt) }

        @MField("rem-int")
        @JvmField
        val remInt: MAny = MFunction { x, y -> x.asInt.rem(y.asInt) }

        @MField("lt-int")
        @JvmField
        val ltInt: MAny = MFunction { x, y -> x.asInt.lt(y.asInt) }

        @MField("gt-int")
        @JvmField
        val gtInt: MAny = MFunction { x, y -> x.asInt.gt(y.asInt) }

        @MField("eq-int")
        @JvmField
        val eqInt: MAny = MFunction { x, y -> x.asInt.eq(y.asInt) }

        @MField("int->char")
        @JvmField
        val intToChar: MAny = MFunction { x -> MChar(x.asInt.value.toChar()) }
    }
}