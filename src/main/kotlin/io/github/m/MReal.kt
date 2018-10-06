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
        override val type = MKeyword("real")

        @JvmStatic
        fun valueOf(value: Float) = MReal(value)
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("add-real")
        @JvmField
        val addReal: MAny = MFunction { x, y -> Cast.toReal(x).add(Cast.toReal(y)) }

        @MField("sub-real")
        @JvmField
        val subReal: MAny = MFunction { x, y -> Cast.toReal(x).sub(Cast.toReal(y)) }

        @MField("mul-real")
        @JvmField
        val mulReal: MAny = MFunction { x, y -> Cast.toReal(x).mul(Cast.toReal(y)) }

        @MField("div-real")
        @JvmField
        val divReal: MAny = MFunction { x, y -> Cast.toReal(x).div(Cast.toReal(y)) }

        @MField("rem-real")
        @JvmField
        val remReal: MAny = MFunction { x, y -> Cast.toReal(x).rem(Cast.toReal(y)) }

        @MField("lt-real")
        @JvmField
        val ltReal: MAny = MFunction { x, y -> Cast.toReal(x).lt(Cast.toReal(y)) }

        @MField("gt-real")
        @JvmField
        val gtReal: MAny = MFunction { x, y -> Cast.toReal(x).gt(Cast.toReal(y)) }

        @MField("eq-real")
        @JvmField
        val eqReal: MAny = MFunction { x, y -> Cast.toReal(x).eq(Cast.toReal(y)) }

        @MField("r")
        @JvmField
        val r: MAny = MFunction { x -> valueOf(Cast.toKeyword(x).value.toFloat()) }
    }
}