@file:JvmName("MString")

package io.github.m

val String.toList get(): List = List.valueOf(asSequence().map(::Char))

val Value.toString get() = String(List.from(this).toList().map { Char.from(it).value }.toCharArray())

fun String.normalize() = map {
    if (it in 'a'..'z') "$it" else "_${it.toInt()}"
}.joinToString("", "", "")
        .let { if (it.isEmpty()) "__" else it }

fun String.unnormalize() = run {
    fun impl(rest: Sequence<kotlin.Char>): Sequence<kotlin.Char> = when {
        rest.none() -> rest
        rest.car == '_' -> {
            val tail = rest.cdr
            val value = tail.dropWhile { it in '0'..'9' }.joinToString("", "", "").toInt().toChar()
            value.cons(tail)
        }
        else -> rest.car.cons(impl(rest.cdr))
    }

    impl(asSequence().asCons()).joinToString("", "", "")
}