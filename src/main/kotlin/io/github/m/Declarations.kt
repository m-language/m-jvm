package io.github.m

/**
 * M declaration definitions.
 */
@Suppress("unused")
@ExperimentalUnsignedTypes
object Declarations {
    @MField("def-declaration")
    @JvmField
    val def: Value = Function { name, path, value -> Declaration.Def(name as List, path as List, value as Operation) }

    @MField("lambda-declaration")
    @JvmField
    val lambda: Value = Function { name, closures, value -> Declaration.Lambda(name as List, closures as List, value as Operation) }

    @MField("combine-declaration")
    @JvmField
    val combine: Value = Function { first, second -> Declaration.Combine(first as Declaration, second as Declaration) }

    @MField("no-declaration")
    @JvmField
    val none: Value = Declaration.None
}
