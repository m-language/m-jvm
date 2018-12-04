package io.github.m

/**
 * Efficient sequence implementation.
 */
sealed class Seq<out A : Any> : Iterable<A> {
    data class Cons<out A : Any>(val car: A, val cdr: Seq<A>) : Seq<A>()
    object Nil : Seq<Nothing>()

    override fun iterator() = run {
        var list = this@Seq
        generateSequence {
            (list as? Seq.Cons)?.run {
                list = cdr
                car
            }
        }.iterator()
    }

    companion object {
        fun <A : Any> valueOf(sequence: Sequence<A>) = sequence
                .toList()
                .foldRight(Seq.Nil as Seq<A>) { car, cdr -> Cons(car, cdr) }
    }
}