package io.github.m

/**
 * M wrapper class for integers.
 */
data class Int(val value: kotlin.Int) : Value {
    override val type get() = Companion.type

    fun add(i: Int) = Int(value + i.value)
    fun sub(i: Int) = Int(value - i.value)
    fun mul(i: Int) = Int(value * i.value)
    fun div(i: Int) = Int(value / i.value)
    fun rem(i: Int) = Int(value % i.value)
    fun lt(i: Int) = Bool.valueOf(value < i.value)
    fun gt(i: Int) = Bool.valueOf(value > i.value)
    fun eq(i: Int) = Bool.valueOf(value == i.value)

    override fun toString() = value.toString()

    companion object : Value {
        /**
         * The type of all integers.
         */
        override val type = Symbol("int")

        @JvmStatic
        fun valueOf(value: kotlin.Int) = Int(value)
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("add-int")
        @JvmField
        val addInt: Value = Function { x, y -> x.asInt.add(y.asInt) }

        @MField("sub-int")
        @JvmField
        val subInt: Value = Function { x, y -> x.asInt.sub(y.asInt) }

        @MField("mul-int")
        @JvmField
        val mulInt: Value = Function { x, y -> x.asInt.mul(y.asInt) }

        @MField("div-int")
        @JvmField
        val divInt: Value = Function { x, y -> x.asInt.div(y.asInt) }

        @MField("rem-int")
        @JvmField
        val remInt: Value = Function { x, y -> x.asInt.rem(y.asInt) }

        @MField("lt-int")
        @JvmField
        val ltInt: Value = Function { x, y -> x.asInt.lt(y.asInt) }

        @MField("gt-int")
        @JvmField
        val gtInt: Value = Function { x, y -> x.asInt.gt(y.asInt) }

        @MField("eq-int")
        @JvmField
        val eqInt: Value = Function { x, y -> x.asInt.eq(y.asInt) }

        @MField("int->char")
        @JvmField
        val intToChar: Value = Function { x -> Char(x.asInt.value.toChar()) }
    }
}