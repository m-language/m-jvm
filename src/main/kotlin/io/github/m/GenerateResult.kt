package io.github.m

import io.github.m.asm.Declaration
import io.github.m.asm.Operation

/**
 * Class representing an M generate result.
 */
data class GenerateResult(val operation: Operation, val declaration: Declaration, val env: Env) : Data {
    override val type get() = Companion.type

    override fun get(key: Symbol) = when (key.value) {
        "operation" -> operation
        "declaration" -> declaration
        "env" -> env
        else -> noField(key)
    }

    companion object : Value {
        override val type = Symbol("generate-result")
    }

    @Suppress("unused")
    object Definitions {
        @MField("generate-result")
        @JvmField
        val generateResult: Value = Function { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            val list3 = list2.cdr.asCons
            GenerateResult(list.car.cast(), list2.car.cast(), list3.car.cast())
        }
    }
}