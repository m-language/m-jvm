@file:JvmName("MString")

package io.github.m

val String.toList get(): List = List.valueOf(asSequence().map(::Char))

val Value.toString get() = String((this as List).toList().map { Char.from(it).value }.toCharArray())

fun String.normalize() = map {
    if (it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' ||it in "_$") "$it" else "\$${it.toInt()}"
}.joinToString("", "", "")