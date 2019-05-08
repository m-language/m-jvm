package io.github.m

/**
 * A declaration for a class.
 */
interface Declaration : Value {
    val name: List
    val path: List

    data class Def(override val name: List, override val path: List, val _value: Operation) : Data.Abstract("def-declaration", "name" to name, "path" to path, "value" to _value), Declaration
    data class Fn(override val name: List, override val path: List, val closures: List, val _value: Operation) : Data.Abstract("fn-declaration", "name" to name, "path" to path, "closures" to closures, "value" to _value), Declaration

    /**
     * M declaration definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField(name = "def-declaration")
        @JvmField
        val def: Value = Value.Impl3("def-declaration") { name, path, value ->
            Def(List.from(name), List.from(path), value as Operation)
        }

        @MField(name = "fn-declaration")
        @JvmField
        val fn: Value = Value.Impl4("fn-declaration") { name, path, closures, value ->
            Fn(List.from(name), List.from(path), List.from(closures), value as Operation)
        }
    }
}