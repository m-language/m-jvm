package io.github.m

/**
 * An operation for a method.
 */
interface Operation : Value {
    data class LocalVariable(val name: List, val index: Nat) : Data.Abstract("local-variable-operation", "name" to name, "index" to index), Operation
    data class GlobalVariable(val name: List, val path: List) : Data.Abstract("global-variable-operation", "name" to name, "path" to path), Operation
    data class If(val cond: Operation, val `true`: Operation, val `false`: Operation) : Data.Abstract("if-operation", "cond" to cond, "true" to `true`, "false" to `false`), Operation
    data class Def(val name: List, val path: List, val _value: Operation) : Data.Abstract("def-operation", "name" to name, "value" to _value, "path" to path), Operation
    data class Fn(val path: List, val name: List, val arg: List, val _value: Operation, val closures: List) : Data.Abstract("fn-operation", "path" to path, "name" to name, "arg" to arg, "value" to _value, "closures" to closures), Operation
    data class Symbol(val name: List) : Data.Abstract("symbol-operation", "name" to name), Operation
    data class Apply(val fn: Operation, val arg: Operation) : Data.Abstract("apply-operation", "fn" to fn, "arg" to arg), Operation
    data class LineNumber(val operation: Operation, val line: Nat) : Data.Abstract("line-number-operation", "operation" to operation, "line" to line), Operation

    /**
     * M operation definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField(name = "local-variable-operation")
        @JvmField
        val localVariable: Value = Value.Impl2("local-variable-operation") { name, index ->
            LocalVariable(List.from(name), Nat.from(index))
        }

        @MField(name = "global-variable-operation")
        @JvmField
        val globalVariable: Value = Value.Impl2("global-variable-operation") { name, path ->
            GlobalVariable(List.from(name), List.from(path))
        }

        @MField(name = "if-operation")
        @JvmField
        val `if`: Value = Value.Impl3("if-operation") { cond, `true`, `false` ->
            If(cond as Operation, `true` as Operation, `false` as Operation)
        }

        @MField(name = "def-operation")
        @JvmField
        val def: Value = Value.Impl3("def-operation") { name, path, value ->
            Def(List.from(name), List.from(path), value as Operation)
        }

        @MField(name = "fn-operation")
        @JvmField
        val fn: Value = Value.Impl5("fn-operation") { path, name, arg, value, closures ->
            Fn(List.from(path), List.from(name), List.from(arg), value as Operation, List.from(closures))
        }

        @MField(name = "symbol-operation")
        @JvmField
        val symbol: Value = Value.Impl1("symbol-operation") { name ->
            Symbol(List.from(name))
        }

        @MField(name = "apply-operation")
        @JvmField
        val apply: Value = Value.Impl2("apply-operation") { fn, arg ->
            Apply(fn as Operation, arg as Operation)
        }

        @MField(name = "line-number-operation")
        @JvmField
        val lineNumber: Value = Value.Impl2("line-number-operation") { operation, line ->
            LineNumber(operation as Operation, Nat.from(line))
        }
    }
}