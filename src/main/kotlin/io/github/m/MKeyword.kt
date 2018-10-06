package io.github.m

/**
 * M wrapper class for strings.
 */
data class MKeyword(val value: String) : MAny {
    override val type get() = Companion.type

    fun eq(k: MKeyword) = MBool.valueOf(value == k.value)

    fun add(k: MKeyword) = MKeyword(value + k.value)

    override fun toString() = value

    companion object : MAny {
        /**
         * The type of all keywords.
         */
        override val type = MKeyword("keyword")
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("eq-keyword")
        @JvmField
        val eqKeyword: MAny = MFunction { x, y -> Cast.toKeyword(x).eq(Cast.toKeyword(y)) }

        @MField("add-keyword")
        @JvmField
        val addKeyword: MAny = MFunction { x, y -> Cast.toKeyword(x).add(Cast.toKeyword(y)) }
    }
}