package io.github.m

/**
 * The superclass of all M values.
 */
interface MAny {
    /**
     * The type of the value.
     */
    val type: MKeyword

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("type-name")
        @JvmField
        val typeName: MAny = MFunction(MAny::type)
    }
}