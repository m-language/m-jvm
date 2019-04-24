package io.github.m

/**
 * M wrapper class for either.
 */
sealed class Either : Value {
    /**
     * The left value of either.
     */
    data class Left(val value: Value) : Either() {
        override fun invoke(arg: Value) = Value { arg2 -> this(arg, arg2) }
        override fun invoke(arg1: Value, arg2: Value) = arg1(value)
    }

    /**
     * The right value of either.
     */
    data class Right(val value: Value) : Either() {
        override fun invoke(arg: Value) = Value { arg2 -> this(arg, arg2) }
        override fun invoke(arg1: Value, arg2: Value) = arg2(value)
    }

    companion object {
        fun from(value: Value) = value as? Either ?: value(Value(::Left), Value(::Right)) as Either
    }

    /**
     * M either definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField(name = "left")
        @JvmField
        val left: Value = Value.Impl1 { x -> Left(x) }

        @MField(name = "right")
        @JvmField
        val right: Value = Value.Impl1 { x -> Right(x) }

        @MField(name = "left?")
        @JvmField
        val isLeft: Value = Value.Impl1 { x -> Bool(from(x) is Left) }

        @MField(name = "right?")
        @JvmField
        val isRight: Value = Value.Impl1 { x -> Bool(from(x) is Right) }
    }
}