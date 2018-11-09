package io.github.m

import io.github.m.asm.Declaration
import io.github.m.asm.Operation

/**
 * The result of generating an expression.
 *
 * @param operation   The operation for the expression.
 * @param declaration The declaration for the expression.
 * @param env         The new environment after this expression.
 */
data class GenerateResult(val operation: Operation, val declaration: Declaration, val env: Env) : MData {
    override val type get() = Companion.type

    override fun get(key: MSymbol) = when (key.value) {
        "operation" -> operation
        "declaration" -> declaration
        "env" -> env
        else -> noField(key)
    }

    companion object : MAny {
        override val type = MSymbol("generate-result")
    }

    @Suppress("unused")
    object Definitions {
        @MField("generate-result")
        @JvmField
        val generateResult: MAny = MFunction { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            val list3 = list2.cdr.asCons
            GenerateResult(list.car.cast(), list2.car.cast(), list3.car.cast())
        }
    }
}