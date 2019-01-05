@file:JvmName("MString")

package io.github.m

val String.toList get(): List = List.valueOf(asSequence().map(::Char))

val Value.toString get() = String((this as List).toList().map { Char.from(it).value }.toCharArray())