package io.github.m

/**
 * M wrapper class for data.
 */
interface Data : Value.Delegate {
    /**
     * The type of the data.
     */
    val type: Symbol

    /**
     * Returns the value of the field with [name].
     */
    fun get(name: Symbol): Value?

    override fun value() = Pair(type, Value.Impl1 { name -> get(Symbol.from(name)) ?: throw Error("No field $name") })

    /**
     * An abstract implementation of data.
     *
     * @param type   The type of the data.
     * @param fields A map of fields representing the data.
     */
    abstract class Abstract(final override val type: Symbol, val fields: Map<String, Value>) : Data {
        constructor(type: String, vararg fields: kotlin.Pair<String, Value>) : this(Symbol.valueOf(type), fields.toMap())
        override fun get(name: Symbol) = fields[name.value]
        override fun toString() = "$type$fields"
    }

    object Definitions {
        @MField(name = "data")
        @JvmField
        val data: Value = Value.Impl2 { type, fields ->
            val fieldsMap = List.from(fields)
                    .map {
                        val pair = Pair.from(it)
                        Symbol.from(pair.first).value to pair.second
                    }
                    .toMap()
            object : Abstract((type as Symbol), fieldsMap) { }
        }
    }
}