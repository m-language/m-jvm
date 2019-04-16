@file:JvmName("MString")

package io.github.m

val String.toList get(): List = List.valueOf(asSequence().map(::Char))

val Value.toString get() = String(List.from(this).toList().map { Char.from(it).char }.toCharArray())

fun String.normalize() = map {
    if (it in 'a'..'z' || it == '_') "$it" else "$${it.toInt()}"
}.joinToString("", "", "")
        .let { if (it.isEmpty()) "__" else it }

fun String.unnormalize() = run {
    fun impl(rest: Sequence<kotlin.Char>): Sequence<kotlin.Char> = when {
        rest.none() -> rest
        rest.car == '$' -> {
            val tail = rest.cdr
            val str = tail.takeWhile { it in '0'..'9' }.joinToString("", "", "")
            val value = str.takeUnless { it.isEmpty() }?.toInt()?.toChar() ?: '$'
            value.cons(impl(tail.drop(str.length)))
        }
        else -> rest.car.cons(impl(rest.cdr))
    }

    impl(asSequence().asCons()).joinToString("", "", "")
}