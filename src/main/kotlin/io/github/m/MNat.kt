package io.github.m

/**
 * M wrapper class for unsigned integers.
 */
@Suppress("DataClassPrivateConstructor")
data class MNat private constructor(val value: Int) : MAny {
    override val type get() = Companion.type

    fun add(n: MNat) = MNat(value + n.value)
    fun sub(n: MNat) = MNat(value - n.value)
    fun mul(n: MNat) = MNat(value * n.value)
    fun div(n: MNat) = MNat(value / n.value)
    fun rem(n: MNat) = MNat(value % n.value)
    fun lt(n: MNat) = MBool.valueOf(value < n.value)
    fun gt(n: MNat) = MBool.valueOf(value > n.value)
    fun eq(n: MNat) = MBool.valueOf(value == n.value)

    override fun toString() = value.toString()

    companion object : MAny {
        /**
         * The type of all naturals.
         */
        override val type = MKeyword("nat")

        @JvmStatic
        fun valueOf(value: Int) = MNat(value)
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("add-nat")
        @JvmField
        val addNat: MAny = MFunction { x, y -> Cast.toNat(x).add(Cast.toNat(y)) }

        @MField("sub-nat")
        @JvmField
        val subNat: MAny = MFunction { x, y -> Cast.toNat(x).sub(Cast.toNat(y)) }

        @MField("mul-nat")
        @JvmField
        val mulNat: MAny = MFunction { x, y -> Cast.toNat(x).mul(Cast.toNat(y)) }

        @MField("div-nat")
        @JvmField
        val divNat: MAny = MFunction { x, y -> Cast.toNat(x).div(Cast.toNat(y)) }

        @MField("rem-nat")
        @JvmField
        val remNat: MAny = MFunction { x, y -> Cast.toNat(x).rem(Cast.toNat(y)) }

        @MField("lt-nat")
        @JvmField
        val ltNat: MAny = MFunction { x, y -> Cast.toNat(x).lt(Cast.toNat(y)) }

        @MField("gt-nat")
        @JvmField
        val gtNat: MAny = MFunction { x, y -> Cast.toNat(x).gt(Cast.toNat(y)) }

        @MField("eq-nat")
        @JvmField
        val eqNat: MAny = MFunction { x, y -> Cast.toNat(x).eq(Cast.toNat(y)) }

        @MField("keyword->nat")
        @JvmField
        val keywordToNat: MAny = MFunction { x -> MNat(Cast.toKeyword(x).value.toInt()) }
    }
}