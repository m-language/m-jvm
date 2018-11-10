@file:JvmName("MString")

package io.github.m

val String.m get(): List = List.valueOf(asSequence().map(::Char))

val List.string get(): String = String(toList().map { it.asChar.value }.toCharArray())

val Value.asString get() = asList.string