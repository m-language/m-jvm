package io.github.m

/**
 * M wrapper for floating points.
 */
data class MReal(val value: Float) : MAny {
    override val type get() = Companion.type

    fun add(r: MReal) = MReal(value + r.value)
    fun sub(r: MReal) = MReal(value - r.value)
    fun mul(r: MReal) = MReal(value * r.value)
    fun div(r: MReal) = MReal(value / r.value)
    fun rem(r: MReal) = MReal(value % r.value)
    fun lt(r: MReal) = MBool.valueOf(value < r.value)
    fun gt(r: MReal) = MBool.valueOf(value > r.value)
    fun eq(r: MReal) = MBool.valueOf(value == r.value)

    override fun toString() = value.toString()

    companion object : MAny {
        /**
         * The type of all reals.
         */
        override val type = MSymbol("real")

        @JvmStatic
        fun valueOf(value: Float) = MReal(value)
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("add-real")
        @JvmField
        val addReal: MAny = MFunction { x, y -> x.asReal.add(y.asReal) }

        @MField("sub-real")
        @JvmField
        val subReal: MAny = MFunction { x, y -> x.asReal.sub(y.asReal) }

        @MField("mul-real")
        @JvmField
        val mulReal: MAny = MFunction { x, y -> x.asReal.mul(y.asReal) }

        @MField("div-real")
        @JvmField
        val divReal: MAny = MFunction { x, y -> x.asReal.div(y.asReal) }

        @MField("rem-real")
        @JvmField
        val remReal: MAny = MFunction { x, y -> x.asReal.rem(y.asReal) }

        @MField("lt-real")
        @JvmField
        val ltReal: MAny = MFunction { x, y -> x.asReal.lt(y.asReal) }

        @MField("gt-real")
        @JvmField
        val gtReal: MAny = MFunction { x, y -> x.asReal.gt(y.asReal) }

        @MField("eq-real")
        @JvmField
        val eqReal: MAny = MFunction { x, y -> x.asReal.eq(y.asReal) }
    }
}