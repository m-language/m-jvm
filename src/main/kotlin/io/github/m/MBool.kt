package io.github.m

/**
 * M wrapper class for booleans.
 */
sealed class MBool(val value: Boolean) : MAny {
    override val type get() = Companion.type

    override fun toString(): String = java.lang.Boolean.toString(value)

    companion object : MAny {
        /**
         * The type of all booleans.
         */
        override val type = MSymbol("bool")

        @Suppress("NOTHING_TO_INLINE")
        @JvmStatic
        inline fun valueOf(b: Boolean) = if (b) True else False
    }

    /**
     * The singleton true object.
     */
    object True : MBool(true)

    /**
     * The singleton false object.
     */
    object False : MBool(false)

    @Suppress("unused")
    object Definitions {
        @MField("true")
        @JvmField
        val `true`: MAny = True

        @MField("false")
        @JvmField
        val `false`: MAny = False
    }
}