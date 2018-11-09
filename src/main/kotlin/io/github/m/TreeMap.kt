package io.github.m

/**
 * Class representing an M tree map.
 */
data class TreeMap(val node: Node, val compare: Function) : Data {
    override val type get() = Companion.type

    override fun get(key: Symbol) = when (key.value) {
        "node" -> node
        "compare" -> compare
        else -> noField(key)
    }

    fun getValue(key: Value) = node.getValue(key, compare)
    fun putValue(key: Value, value: Value) = copy(node = node.putValue(key, value, compare))

    companion object : Value {
        override val type = Symbol("tree-map")

        fun empty(compare: Function) = TreeMap(Node.Nil, compare)
    }

    sealed class Node : Data {
        fun getValue(key: Value, compare: Function): Maybe = when (this) {
            Nil -> Maybe.None
            is Cons -> {
                val result: Compare = compare(key, this.key).cast()
                when (result) {
                    Compare.Greater -> right.getValue(key, compare)
                    Compare.Less -> left.getValue(key, compare)
                    Compare.Equals -> Maybe.Some(value)
                }
            }
        }

        fun putValue(key: Value, value: Value, compare: Function): Node = when (this) {
            Nil -> Cons(Nil, Nil, key, value)
            is Cons -> {
                val result: Compare = compare(key, this.key).cast()
                when (result) {
                    Compare.Greater -> copy(right = right.putValue(key, value, compare))
                    Compare.Less -> copy(left = left.putValue(key, value, compare))
                    Compare.Equals -> copy(key = key, value = value)
                }
            }
        }

        object Nil : Node() {
            override val type = Symbol("tree-map-node-nil")
            override fun get(key: Symbol) = noField(key)
            override fun toString() = "nil"
        }

        data class Cons(val left: Node, val right: Node, val key: Value, val value: Value) : Node() {
            override val type get() = Companion.type

            override fun get(key: Symbol) = when (key.value) {
                "left" -> left
                "right" -> right
                "key" -> this.key
                "value" -> value
                else -> noField(key)
            }

            override fun toString() = "($left ${key to value} $right)"

            companion object : Value {
                override val type = Symbol("tree-map-node")
            }
        }
    }

    @Suppress("unused")
    object Definitions {
        @MField("tree-map")
        @JvmField
        val treeMap: Value = Function { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            TreeMap(list.car.cast(), list2.car.cast())
        }

        @MField("tree-map-node")
        @JvmField
        val treeMapNode: Value = Function { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            val list3 = list2.cdr.asCons
            val list4 = list3.cdr.asCons
            Node.Cons(list.car.cast(), list2.car.cast(), list3.car.cast(), list4.car.cast())
        }

        @MField("tree-map-node-nil")
        @JvmField
        val treeMapNodeNil: Value = TreeMap.Node.Nil
    }
}
