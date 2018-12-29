package io.github.m

data class ConsSequence<A>(val head: A, val tail: Sequence<A>) : Sequence<A> {
    override fun iterator(): Iterator<A> = iterator {
        yield(head)
        yieldAll(tail)
    }
}

fun <A> nil() = emptySequence<A>()

fun <A> Sequence<A>.asCons() = toList().foldRight(nil<A>()) { b, a -> ConsSequence(b, a) }

fun <A> A.cons(sequence: Sequence<A>) = ConsSequence(this, sequence)

val <A> Sequence<A>.car get() = if (this is ConsSequence<A>) head else first()
val <A> Sequence<A>.cdr get() = if (this is ConsSequence<A>) tail else drop(1)

fun <A> Sequence<A>.reversed() = asIterable().reversed()