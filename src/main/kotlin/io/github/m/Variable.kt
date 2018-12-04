package io.github.m

/**
 * Class representing an M variable.
 */
@ExperimentalUnsignedTypes
sealed class Variable : Data {
    data class Local(val name: List, val index: Nat) : Variable() {
        override val type = "local-variable"
        override fun get(name: String) = when (name) {
            "name" -> this.name
            "index" -> this.index
            else -> null
        }
    }

    data class Global(val name: List, val path: List) : Variable() {
        override val type = "global-variable"
        override fun get(name: String) = when (name) {
            "name" -> this.name
            "path" -> this.path
            else -> null
        }
    }

    /**
     * M variable definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("local-variable")
        @JvmField
        val local: Value = Function { name, index -> Variable.Local(name as List, index as Nat) }

        @MField("global-variable")
        @JvmField
        val global: Value = Function { name, file -> Variable.Global(name as List, file as List) }
    }
}