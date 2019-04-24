@file:JvmName("MString")

package io.github.m

val String.toList get(): List = asSequence().map(Char::valueOf).list()

val Value.toString get() = String(List.from(this).toList().map { Char.from(it).value }.toCharArray())

fun String.normalize() = Symbol.normalize(this)

fun String.unnormalize() = Symbol.unnormalize(this)