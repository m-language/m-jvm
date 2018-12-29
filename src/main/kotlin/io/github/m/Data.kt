package io.github.m

/**
 * M wrapper class for data.
 */
interface Data : Pair {
    /**
     * The type of the data.
     */
    val type: Symbol

    /**
     * Returns the value of the field with [name].
     */
    fun get(name: Symbol): Value?

    override val left get() = type
    override val right get() = Function { name -> get(name as Symbol) ?: throw Error("No field $name") }

    /**
     * An abstract implementation of data.
     *
     * @param type   The type of the data.
     * @param fields A map of fields representing the data.
     */
    abstract class Abstract(final override val type: Symbol, val fields: Map<Symbol, Value>) : Data {
        constructor(type: String, vararg fields: kotlin.Pair<String, Value>) : this(Symbol(type), fields.toMap().mapKeys { Symbol(it.key) })
        override fun get(name: Symbol) = fields[name]
        override fun toString() = "$type$fields"
    }

    object Definitions {
        @MField("data")
        @JvmField
        val data: Value = Function { type, fields ->
            val fieldsMap = (fields as List)
                    .map {
                        it as Pair
                        it.left as Symbol to it.right
                    }
                    .toMap()
            object : Abstract((type as Symbol), fieldsMap) { }
        }
    }
}