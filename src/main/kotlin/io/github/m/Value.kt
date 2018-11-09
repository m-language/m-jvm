package io.github.m

/**
 * The superclass of all M values.
 */
interface Value {
    /**
     * The type of the value.
     */
    val type: Symbol

    @Suppress("unused")
    object Definitions {
        @MField("type-name")
        @JvmField
        val typeName: Value = Function(Value::type)
    }
}