package io.github.m

/**
 * M wrapper class for data.
 */
@ExperimentalUnsignedTypes
interface Data : Pair {
    /**
     * The type of the data.
     */
    val type: String

    /**
     * Returns the value of the field with [name].
     */
    fun get(name: String): Value?

    override val left get() = Symbol(type)
    override val right get() = Function { name -> get((name as Symbol).value) ?: throw Error("No field $name") }

    /**
     * An abstract implementation of data.
     *
     * @param type   The type of the data.
     * @param fields A map of fields representing the data.
     */
    abstract class Abstract(final override val type: String, val fields: Map<String, Value>) : Data {
        constructor(type: String, vararg fields: kotlin.Pair<String, Value>) : this(type, fields.toMap())
        override fun get(name: String) = fields[name]
        override fun toString() = "$type$fields"
    }
}