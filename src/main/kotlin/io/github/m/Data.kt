package io.github.m

/**
 * Representation of data in M.
 */
interface Data : Value {
    /**
     * Gets the value of a field.
     *
     * @param key The key for the field.
     */
    operator fun get(key: Symbol): Value

    /**
     * Utility def for failing if a field does not exist.
     *
     * @param key The key for the field.
     */
    @JvmDefault
    fun noField(key: Symbol): Nothing = throw Error.NoField(key, type)

    /**
     * A generic implementation of data.
     *
     * @param type   The type of the data.
     * @param fields A map of fields representing the data
     */
    class Impl(override val type: Symbol, private val fields: Map<Symbol, Value>) : Data {
        override fun get(key: Symbol) = fields[key] ?: noField(key)
        override fun toString() = "$type($fields)"
    }

    companion object : Value {
        override val type = Symbol("data")
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("new-data")
        @JvmField
        val newData: Value = Function { name, fields, values ->
            val fields0 = fields.asList.asSequence().map { it.asSymbol }
            val values0 = values.asList.asSequence()
            Impl(name.asSymbol, (fields0 zip values0).toMap())
        }

        @MField("field")
        @JvmField
        val field: Value = Function { type, name, data -> data.asData(type.asSymbol)[name.asSymbol] }
    }
}
