package io.github.m

/**
 * M wrapper class for bools.
 */
sealed class Bool : Value {
    object True : Bool() {
        override fun toString() = "true"
    }

    object False : Bool() {
        override fun toString() = "false"
    }

    companion object {
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