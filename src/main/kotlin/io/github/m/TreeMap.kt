package io.github.m

/**
 * Class representing an M tree map.
 */
data class TreeMap(val node: Node, val compare: MFunction) : MData {
    override val type get() = Companion.type

    override fun get(key: MSymbol) = when (key.value) {
        "node" -> node
        "compare" -> compare
        else -> noField(key)
    }

    fun getValue(key: MAny) = node.getValue(key, compare)
    fun putValue(key: MAny, value: MAny) = copy(node = node.putValue(key, value, compare))

    companion object : MAny {
        override val type = MSymbol("tree-map")

        fun empty(compare: MFunction) = TreeMap(Node.Nil, compare)
    }

    sealed class Node : MData {
        fun getValue(key: MAny, compare: MFunction): Maybe = when (this) {
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

        fun putValue(key: MAny, value: MAny, compare: MFunction): Node = when (this) {
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
            override val type = MSymbol("tree-map-node-nil")
            override fun get(key: MSymbol) = noField(key)
            override fun toString() = "nil"
        }

        data class Cons(val left: Node, val right: Node, val key: MAny, val value: MAny) : Node() {
            override val type get() = Companion.type

            override fun get(key: MSymbol) = when (key.value) {
                "left" -> left
                "right" -> right
                "key" -> this.key
                "value" -> value
                else -> noField(key)
            }

            override fun toString() = "($left ${key to value} $right)"

            companion object : MAny {
                override val type = MSymbol("tree-map-node")
            }
        }
    }

    @Suppress("unused")
    object Definitions {
        @MField("tree-map")
        @JvmField
        val treeMap: MAny = MFunction { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            TreeMap(list.car.cast(), list2.car.cast())
        }

        @MField("tree-map-node")
        @JvmField
        val treeMapNode: MAny = MFunction { fields ->
            val list = fields.asCons
            val list2 = list.cdr.asCons
            val list3 = list2.cdr.asCons
            val list4 = list3.cdr.asCons
            Node.Cons(list.car.cast(), list2.car.cast(), list3.car.cast(), list4.car.cast())
        }

        @MField("tree-map-node-nil")
        @JvmField
        val treeMapNodeNil: MAny = TreeMap.Node.Nil
    }
}
