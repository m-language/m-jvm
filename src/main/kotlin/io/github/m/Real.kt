package io.github.m

/**
 * M wrapper for floating points.
 */
data class Real(val value: Float) : Value {
    override val type get() = Companion.type

    fun add(r: Real) = Real(value + r.value)
    fun sub(r: Real) = Real(value - r.value)
    fun mul(r: Real) = Real(value * r.value)
    fun div(r: Real) = Real(value / r.value)
    fun rem(r: Real) = Real(value % r.value)
    fun lt(r: Real) = Bool.valueOf(value < r.value)
    fun gt(r: Real) = Bool.valueOf(value > r.value)
    fun eq(r: Real) = Bool.valueOf(value == r.value)

    override fun toString() = value.toString()

    companion object : Value {
        /**
         * The type of all reals.
         */
        override val type = Symbol("real")

        @JvmStatic
        fun valueOf(value: Float) = Real(value)
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("add-real")
        @JvmField
        val addReal: Value = Function { x, y -> x.asReal.add(y.asReal) }

        @MField("sub-real")
        @JvmField
        val subReal: Value = Function { x, y -> x.asReal.sub(y.asReal) }

        @MField("mul-real")
        @JvmField
        val mulReal: Value = Function { x, y -> x.asReal.mul(y.asReal) }

        @MField("div-real")
        @JvmField
        val divReal: Value = Function { x, y -> x.asReal.div(y.asReal) }

        @MField("rem-real")
        @JvmField
        val remReal: Value = Function { x, y -> x.asReal.rem(y.asReal) }

        @MField("lt-real")
        @JvmField
        val ltReal: Value = Function { x, y -> x.asReal.lt(y.asReal) }

        @MField("gt-real")
        @JvmField
        val gtReal: Value = Function { x, y -> x.asReal.gt(y.asReal) }

        @MField("eq-real")
        @JvmField
        val eqReal: Value = Function { x, y -> x.asReal.eq(y.asReal) }
    }
}