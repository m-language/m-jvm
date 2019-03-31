package io.github.m

/**
 * A declaration for a class.
 */
interface Declaration : Value {
    val name: List
    val path: List

    data class Def(override val name: List, override val path: List, val value: Operation) : Data.Abstract("def-declaration", "name" to name, "path" to path, "value" to value), Declaration
    data class Fn(override val name: List, override val path: List, val closures: List, val value: Operation) : Data.Abstract("fn-declaration", "name" to name, "path" to path, "closures" to closures, "value" to value), Declaration

    /**
     * M declaration definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("def-declaration")
        @JvmField
        val def: Value = Value { name, path, value ->
            Declaration.Def(List.from(name), List.from(path), value as Operation)
        }

        @MField("fn-declaration")
        @JvmField
        val fn: Value = Value { name, path, closures, value ->
            Declaration.Fn(List.from(name), List.from(path), List.from(closures), value as Operation)
        }
    }
}