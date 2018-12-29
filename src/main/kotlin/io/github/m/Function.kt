package io.github.m

/**
 * M implementation of functions.
 */
@FunctionalInterface
interface Function : Value {
    /**
     * Invokes the function with an argument of nil.
     */
    @JvmDefault
    operator fun invoke() = invoke(List.Nil)

    /**
     * Invokes the function given an [arg].
     */
    operator fun invoke(arg: Value): Value

    @JvmDefault
    operator fun invoke(arg1: Value, arg2: Value) = (this(arg1) as Function)(arg2)

    @JvmDefault
    operator fun invoke(arg1: Value, arg2: Value, arg3: Value) = (this(arg1, arg2) as Function)(arg3)

    @JvmDefault
    operator fun invoke(arg1: Value, arg2: Value, arg3: Value, arg4: Value) = (this(arg1, arg2, arg3) as Function)(arg4)

    @JvmDefault
    operator fun invoke(arg1: Value, arg2: Value, arg3: Value, arg4: Value, arg5: Value) = (this(arg1, arg2, arg3, arg4) as Function)(arg5)

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: () -> Value) = Impl0(fn)

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: (Value) -> Value) = Impl1(fn)

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: (Value, Value) -> Value) = Impl2(fn)

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: (Value, Value, Value) -> Value) = Impl3(fn)

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: (Value, Value, Value, Value) -> Value) = Impl4(fn)

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: (Value, Value, Value, Value, Value) -> Value) = Impl5(fn)
    }

    class Impl0(val fn: () -> Value) : Function {
        override fun invoke(): Value = fn()
        override fun invoke(arg: Value): Value = fn()
    }

    class Impl1(val fn: (Value) -> Value) : Function {
        override fun invoke(arg: Value): Value = fn(arg)
    }

    class Impl2(val fn: (Value, Value) -> Value) : Function {
        override fun invoke(arg: Value): Value = Impl1 { arg1 -> fn(arg, arg1) }
        override fun invoke(arg1: Value, arg2: Value) = fn(arg1, arg2)
    }

    class Impl3(val fn: (Value, Value, Value) -> Value) : Function {
        override fun invoke(arg: Value): Value = Impl2 { arg1, arg2 -> fn(arg, arg1, arg2) }
        override fun invoke(arg1: Value, arg2: Value, arg3: Value) = fn(arg1, arg2, arg3)
    }

    class Impl4(val fn: (Value, Value, Value, Value) -> Value) : Function {
        override fun invoke(arg: Value): Value = Impl3 { arg1, arg2, arg3 -> fn(arg, arg1, arg2, arg3) }
        override fun invoke(arg1: Value, arg2: Value, arg3: Value, arg4: Value) = fn(arg1, arg2, arg3, arg4)
    }

    class Impl5(val fn: (Value, Value, Value, Value, Value) -> Value) : Function {
        override fun invoke(arg: Value): Value = Impl4 { arg1, arg2, arg3, arg4 -> fn(arg, arg1, arg2, arg3, arg4) }
        override fun invoke(arg1: Value, arg2: Value, arg3: Value, arg4: Value, arg5: Value) = fn(arg1, arg2, arg3, arg4, arg5)
    }

    /**
     * M function definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("function->process")
        @JvmField
        val toProcess: Value = Function { fn -> Process { (fn as Function)() } }
    }
}