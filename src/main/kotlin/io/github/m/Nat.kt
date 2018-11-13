package io.github.m

/**
 * M wrapper class for natural numbers.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
data class Nat(val value: kotlin.UInt) : Value {
    override val type get() = Companion.type

    fun add(i: Nat) = Nat(value + i.value)
    fun sub(i: Nat) = Nat(value - i.value)
    fun mul(i: Nat) = Nat(value * i.value)
    fun div(i: Nat) = Nat(value / i.value)
    fun rem(i: Nat) = Nat(value % i.value)
    fun lt(i: Nat) = Bool.valueOf(value < i.value)
    fun gt(i: Nat) = Bool.valueOf(value > i.value)
    fun eq(i: Nat) = Bool.valueOf(value == i.value)

    override fun toString() = value.toString()

    companion object : Value {
        /**
         * The type of all integers.
         */
        override val type = Symbol("nat")

        @JvmStatic
        fun valueOf(value: kotlin.UInt) = Nat(value)
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("add-nat")
        @JvmField
        val addNat: Value = Function { x, y -> x.asNat.add(y.asNat) }

        @MField("sub-nat")
        @JvmField
        val subNat: Value = Function { x, y -> x.asNat.sub(y.asNat) }

        @MField("mul-nat")
        @JvmField
        val mulNat: Value = Function { x, y -> x.asNat.mul(y.asNat) }

        @MField("div-nat")
        @JvmField
        val divNat: Value = Function { x, y -> x.asNat.div(y.asNat) }

        @MField("rem-nat")
        @JvmField
        val remNat: Value = Function { x, y -> x.asNat.rem(y.asNat) }

        @MField("lt-nat")
        @JvmField
        val ltNat: Value = Function { x, y -> x.asNat.lt(y.asNat) }

        @MField("gt-nat")
        @JvmField
        val gtNat: Value = Function { x, y -> x.asNat.gt(y.asNat) }

        @MField("eq-nat")
        @JvmField
        val eqNat: Value = Function { x, y -> x.asNat.eq(y.asNat) }

        @MField("nat->char")
        @JvmField
        val natToChar: Value = Function { x -> Char(x.asNat.value.toInt().toChar()) }
    }
}