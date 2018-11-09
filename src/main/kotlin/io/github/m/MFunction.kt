package io.github.m

/**
 * M implementation for functions.
 */
@FunctionalInterface
interface MFunction : MAny {
    @JvmDefault
    override val type
        get() = Companion.type

    /**
     * Invokes the function with an argument of nil.
     */
    @JvmDefault
    operator fun invoke() = invoke(MList.Nil)

    operator fun invoke(arg: MAny): MAny

    @JvmDefault
    operator fun invoke(arg1: MAny, arg2: MAny) = this(arg1).asFunction(arg2)

    @JvmDefault
    operator fun invoke(arg1: MAny, arg2: MAny, arg3: MAny) = this(arg1, arg2).asFunction(arg3)

    @JvmDefault
    operator fun invoke(arg1: MAny, arg2: MAny, arg3: MAny, arg4: MAny) = this(arg1, arg2, arg3).asFunction(arg4)

    companion object : MAny {
        /**
         * The type of all functions.
         */
        override val type = MSymbol("function")

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: () -> MAny) = Impl0(fn)

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: (MAny) -> MAny) = Impl1(fn)

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: (MAny, MAny) -> MAny) = Impl2(fn)

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: (MAny, MAny, MAny) -> MAny) = Impl3(fn)

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(noinline fn: (MAny, MAny, MAny, MAny) -> MAny) = Impl4(fn)
    }

    class Impl0(val fn: () -> MAny) : MFunction {
        override fun invoke(): MAny = fn()
        override fun invoke(arg: MAny): MAny = fn()
    }

    class Impl1(val fn: (MAny) -> MAny) : MFunction {
        override fun invoke(arg: MAny): MAny = fn(arg)
    }

    class Impl2(val fn: (MAny, MAny) -> MAny) : MFunction {
        override fun invoke(arg: MAny): MAny = Impl1 { arg1 -> fn(arg, arg1) }
        override fun invoke(arg1: MAny, arg2: MAny) = fn(arg1, arg2)
    }

    class Impl3(val fn: (MAny, MAny, MAny) -> MAny) : MFunction {
        override fun invoke(arg: MAny): MAny = Impl2 { arg1, arg2 -> fn(arg, arg1, arg2) }
        override fun invoke(arg1: MAny, arg2: MAny, arg3: MAny) = fn(arg1, arg2, arg3)
    }

    class Impl4(val fn: (MAny, MAny, MAny, MAny) -> MAny) : MFunction {
        override fun invoke(arg: MAny): MAny = Impl3 { arg1, arg2, arg3 -> fn(arg, arg1, arg2, arg3) }
        override fun invoke(arg1: MAny, arg2: MAny, arg3: MAny, arg4: MAny) = fn(arg1, arg2, arg3, arg4)
    }

    @Suppress("unused")
    object Internal {
        @JvmStatic
        fun apply(function: MAny, arg: MAny) =
                try {
                    function.asFunction(arg)
                } catch (e: MError) {
                    throw e
                } catch (e: Throwable) {
                    throw MError.Internal(e)
                }
    }
}