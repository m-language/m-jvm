package io.github.m

/**
 * M operation definitions.
 */
@Suppress("unused")
@ExperimentalUnsignedTypes
object Operations {
    @MField("local-variable-operation")
    @JvmField
    val localVariable: Value = Function { name, index -> Operation.LocalVariable(name as List, index as Nat) }

    @MField("global-variable-operation")
    @JvmField
    val globalVariable: Value = Function { name, path -> Operation.GlobalVariable(name as List, path as List) }

    @MField("reflective-variable-operation")
    @JvmField
    val reflectiveVariable: Value = Function { name, path -> Operation.ReflectiveVariable(name as List, path as List) }

    @MField("if-operation")
    @JvmField
    val `if`: Value = Function { cond, `true`, `false` -> Operation.If(cond as Operation, `true` as Operation, `false` as Operation) }

    @MField("def-operation")
    @JvmField
    val def: Value = Function { name, value, path -> Operation.Def(name as List, value as Operation, path as List) }

    @MField("lambda-operation")
    @JvmField
    val lambda: Value = Function { path, name, closures -> Operation.Lambda(path as List, name as List, closures as List) }

    @MField("symbol-operation")
    @JvmField
    val symbol: Value = Function { name -> Operation.Symbol(name as List) }

    @MField("apply-operation")
    @JvmField
    val apply: Value = Function { fn, arg -> Operation.Apply(fn as Operation, arg as Operation) }

    @MField("combine-operation")
    @JvmField
    val combine: Value = Function { first, second -> Operation.Combine(first as Operation, second as Operation) }

    @MField("line-number-operation")
    @JvmField
    val lineNumber: Value = Function { operation, line -> Operation.LineNumber(operation as Operation, line as Nat) }

    @MField("nil-operation")
    @JvmField
    val nil: Value = Operation.Nil
}