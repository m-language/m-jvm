package io.github.m

/**
 * Class representing an M variable.
 */
@ExperimentalUnsignedTypes
sealed class Variable : Data {
    data class Local(val name: List, val index: Nat) : Variable() {
        override val type = Symbol("local-variable")
        override fun get(name: Symbol) = when (name.value) {
            "name" -> this.name
            "index" -> this.index
            else -> null
        }

        override fun toString() = "${name.toString}@$index"
    }

    data class Global(val name: List, val path: List) : Variable() {
        override val type = Symbol("global-variable")
        override fun get(name: Symbol) = when (name.value) {
            "name" -> this.name
            "path" -> this.path
            else -> null
        }

        override fun toString() = "${path.toString}/${name.toString}"
    }

    /**
     * M variable definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("local-variable")
        @JvmField
        val local: Value = Value { name, index ->
            Variable.Local(List.from(name), Nat.from(index))
        }

        @MField("global-variable")
        @JvmField
        val global: Value = Value { name, file ->
            Variable.Global(List.from(name), List.from(file))
        }
    }
}