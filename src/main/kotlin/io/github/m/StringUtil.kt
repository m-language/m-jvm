@file:JvmName("MString")

package io.github.m

val String.m get(): List = List.valueOf(asSequence().map(::Char))

val Value.asString get() = String(asList.toList().map { it.asChar.value }.toCharArray())