package io.github.m

/**
 * An operation for a method.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
interface Operation : Value {
    data class LocalVariable(val name: List, val index: Nat) : Data.Abstract("local-variable-operation", "name" to name, "index" to index), Operation
    data class GlobalVariable(val name: List, val path: List) : Data.Abstract("global-variable-operation", "name" to name, "path" to path), Operation
    data class If(val cond: Operation, val `true`: Operation, val `false`: Operation) : Data.Abstract("if-operation", "cond" to cond, "true" to `true`, "false" to `false`), Operation
    data class Def(val name: List, val path: List, val value: Operation) : Data.Abstract("def-operation", "name" to name, "value" to value, "path" to path), Operation
    data class Fn(val path: List, val name: List, val arg: List, val value: Operation, val closures: List) : Data.Abstract("fn-operation", "path" to path, "name" to name, "arg" to arg, "value" to value, "closures" to closures), Operation
    data class Symbol(val name: List) : Data.Abstract("symbol-operation", "name" to name), Operation
    data class Apply(val fn: Operation, val arg: Operation) : Data.Abstract("apply-operation", "fn" to fn, "arg" to arg), Operation
    data class LineNumber(val operation: Operation, val line: Nat) : Data.Abstract("line-number-operation", "operation" to operation, "line" to line), Operation
    object Nil : Data.Abstract("nil-operation"), Operation

    /**
     * M operation definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("local-variable-operation")
        @JvmField
        val localVariable: Value = Value { name, index ->
            Operation.LocalVariable(List.from(name), Nat.from(index))
        }

        @MField("global-variable-operation")
        @JvmField
        val globalVariable: Value = Value { name, path ->
            Operation.GlobalVariable(List.from(name), List.from(path))
        }

        @MField("if-operation")
        @JvmField
        val `if`: Value = Value { cond, `true`, `false` ->
            Operation.If(cond as Operation, `true` as Operation, `false` as Operation)
        }

        @MField("def-operation")
        @JvmField
        val def: Value = Value { name, path, value ->
            Operation.Def(List.from(name), List.from(path), value as Operation)
        }

        @MField("fn-operation")
        @JvmField
        val fn: Value = Value { path, name, arg, value, closures ->
            Operation.Fn(List.from(path), List.from(name), List.from(arg), value as Operation, List.from(closures))
        }

        @MField("symbol-operation")
        @JvmField
        val symbol: Value = Value { name ->
            Operation.Symbol(List.from(name))
        }

        @MField("apply-operation")
        @JvmField
        val apply: Value = Value { fn, arg ->
            Operation.Apply(fn as Operation, arg as Operation)
        }

        @MField("line-number-operation")
        @JvmField
        val lineNumber: Value = Value { operation, line ->
            Operation.LineNumber(operation as Operation, Nat.from(line))
        }

        @MField("nil-operation")
        @JvmField
        val nil: Value = Operation.Nil
    }
}