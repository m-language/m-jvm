package io.github.m

/**
 * M wrapper class for bools.
 */
sealed class Bool : Function {
    object True : Bool() {
        override fun invoke(arg: Value) = Function { arg2 -> this(arg, arg2) }
        override fun invoke(arg1: Value, arg2: Value) = arg1
        override fun toString() = "true"
    }

    object False : Bool(), List {
        override fun iterator() = emptySequence<Value>().iterator()
        override fun invoke(arg: Value) = Function { arg2 -> this(arg, arg2) }
        override fun invoke(arg1: Value, arg2: Value) = arg2
        override fun toString() = "false"
    }

    companion object {
        fun from(value: Value) = value as? Bool ?: (value as Function)(True, False) as Bool
        operator fun invoke(boolean: Boolean) = if (boolean) True else False
    }

    /**
     * M bool definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("true")
        @JvmField
        val `true`: Value = Bool.True

        @MField("false")
        @JvmField
        val `false`: Value = Bool.False
    }
}