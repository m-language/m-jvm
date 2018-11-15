package io.github.m

/**
 * M wrapper class for booleans.
 */
sealed class Bool(val value: Boolean) : Value {
    override val type get() = Companion.type

    override fun toString(): String = java.lang.Boolean.toString(value)

    companion object : Value {
        /**
         * The type of all booleans.
         */
        override val type = Symbol("bool")

        @Suppress("NOTHING_TO_INLINE")
        @JvmStatic
        inline fun valueOf(b: Boolean) = if (b) True else False
    }

    /**
     * The singleton true object.
     */
    object True : Bool(true)

    /**
     * The singleton false object.
     */
    object False : Bool(false)

    @Suppress("unused")
    object Definitions {
        @MField("true")
        @JvmField
        val `true`: Value = True

        @MField("false")
        @JvmField
        val `false`: Value = False
    }
}