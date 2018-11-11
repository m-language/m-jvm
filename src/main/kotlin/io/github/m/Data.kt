package io.github.m

import io.github.m.asm.asQualifiedName
import java.lang.reflect.Modifier

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
     * Derives a data object with a key and value.
     *
     * @param key   The key for the derived field.
     * @param value The value for the derived field.
     */
    @JvmDefault
    fun derive(key: Symbol, value: Value): Data = Derive(this, key, value)

    /**
     * A generic implementation of data.
     *
     * @param type   The type of the data.
     * @param fields A map of fields representing the data.
     */
    data class Impl(override val type: Symbol, val fields: Map<Symbol, Value>) : Data {
        override fun get(key: Symbol) = fields[key] ?: noField(key)
        override fun derive(key: Symbol, value: Value) = copy(fields = fields + (key to value))
        override fun toString() = "$type$fields"
    }

    /**
     * A generic implementation of derived data.
     *
     * @param data  The data to derive.
     * @param key   The key for the derived field.
     * @param value The value for the derived field.
     */
    data class Derive(val data: Data, val key: Symbol, val value: Value) : Data {
        override val type get() = data.type
        override fun get(key: Symbol) = if (key == this.key) value else data[key]
        override fun toString() = "$data${key to value}"
    }

    companion object : Value {
        override val type = Symbol("data")
    }

    @Suppress("unused")
    object Definitions {
        @MField("object")
        @JvmField
        val `object`: Value = Function { type ->
            Impl(type.asSymbol, emptyMap())
        }

        @MField("derive")
        @JvmField
        val derive: Value = Function { type, key, value, data ->
            data.asData(type.asSymbol).derive(key.asSymbol, value)
        }

        @MField("field")
        @JvmField
        val field: Value = Function { type, name, data ->
            data.asData(type.asSymbol)[name.asSymbol]
        }

        @MField("import")
        @JvmStatic
        val import = Function { type ->
            val clazz = Class.forName(type.asSymbol.value)
            Impl(
                    Symbol(clazz.name),
                    clazz.fields
                            .asSequence()
                            .filter { Modifier.isStatic(it.modifiers) }
                            .filter { it.type == Value::class.java }
                            .map { Symbol(it.name) to it.get(null) as Value }
                            .toMap()
            )
        }
    }
}
