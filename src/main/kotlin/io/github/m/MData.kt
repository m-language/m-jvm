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
    operator fun get(key: MKeyword): MAny

    /**
     * Utility method for failing if a field does not exist.
     *
     * @param key The key for the field.
     */
    @JvmDefault
    fun noField(key: MKeyword): Nothing = throw MError.NoField(key, type)

    /**
     * A generic implementation of data.
     *
     * @param type   The type of the data.
     * @param fields A map of fields representing the data
     */
    class Impl(override val type: MKeyword, private val fields: Map<MKeyword, MAny>) : MData {
        override fun get(key: MKeyword) = fields[key] ?: noField(key)
        override fun toString() = "$type($fields)"
    }

    companion object : MAny {
        override val type = MKeyword("data")
    }

    @Suppress("unused", "ObjectPropertyName")
    object Definitions {
        @MField("new-data")
        @JvmField
        val newData: MAny = MFunction { name, fields, values ->
            val fields0 = Cast.toList(fields).asSequence().map { Cast.toKeyword(it) }
            val values0 = Cast.toList(values).asSequence()
            Impl(Cast.toKeyword(name), (fields0 zip values0).toMap())
        }

        @MField("field")
        @JvmField
        val field: MAny = MFunction { name, data -> Cast.toData(data)[Cast.toKeyword(name)] }
    }
}
