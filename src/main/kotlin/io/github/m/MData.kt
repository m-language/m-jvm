package io.github.m

/**
 * Representation of data in M.
 */
interface MData : MAny {
    /**
     * Gets the value of a field.
     *
     * @param key The key for the field.
     */
    operator fun get(key: MSymbol): MAny

    /**
     * Utility method for failing if a field does not exist.
     *
     * @param key The key for the field.
     */
    @JvmDefault
    fun noField(key: MSymbol): Nothing = throw MError.NoField(key, type)

    /**
     * A generic implementation of data.
     *
     * @param type   The type of the data.
     * @param fields A map of fields representing the data
     */
    class Impl(override val type: MSymbol, private val fields: Map<MSymbol, MAny>) : MData {
        override fun get(key: MSymbol) = fields[key] ?: noField(key)
        override fun toString() = "$type($fields)"
    }

    companion object : MAny {
        override val type = MSymbol("data")
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("new-data")
        @JvmField
        val newData: MAny = MFunction { name, fields, values ->
            val fields0 = Cast.toList(fields).asSequence().map { Cast.toSymbol(it) }
            val values0 = Cast.toList(values).asSequence()
            Impl(Cast.toSymbol(name), (fields0 zip values0).toMap())
        }

        @MField("field")
        @JvmField
        val field: MAny = MFunction { name, data -> Cast.toData(data)[Cast.toSymbol(name)] }
    }
}
