package io.github.m

interface Tree : Value {
    data class Val(val name: List) : Data.Abstract("tree/val", "name" to name), Tree
    data class Def(val name: List, val _value: Tree) : Data.Abstract("tree/def", "name" to name, "value" to _value), Tree
    data class Fn(val arg: List, val _value: Tree) : Data.Abstract("tree/fn", "arg" to arg, "value" to _value), Tree
    data class Ap(val fn: Tree, val arg: Tree) : Data.Abstract("tree/ap", "fn" to fn, "arg" to arg), Tree
    data class Symbol(val name: List) : Data.Abstract("tree/symbol", "name" to name), Tree

    @Suppress("unused")
    object Definitions {
        @MField(name = "tree/val")
        @JvmField
        val `val`: Value = Value.Impl1("tree/val") { name ->
            Val(List.from(name))
        }

        @MField(name = "tree/def")
        @JvmField
        val def: Value = Value.Impl2("tree/def") { name, value ->
            Def(List.from(name), value as Tree)
        }

        @MField(name = "tree/fn")
        @JvmField
        val fn: Value = Value.Impl2("tree/fn") { arg, value ->
            Fn(List.from(arg), value as Tree)
        }

        @MField(name = "tree/ap")
        @JvmField
        val ap: Value = Value.Impl2("tree/ap") { fn, arg ->
            Ap(fn as Tree, arg as Tree)
        }

        @MField(name = "tree/symbol")
        @JvmField
        val symbol: Value = Value.Impl1("tree/symbol") { name ->
            Symbol(List.from(name))
        }
    }
}