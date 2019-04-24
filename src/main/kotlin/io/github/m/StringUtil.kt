@file:JvmName("MString")

package io.github.m

val String.toList get(): List = Symbol.toList(this)

val Value.toString get() = Symbol.toString(List.from(this))

fun String.normalize() = Symbol.normalize(this)

fun String.unnormalize() = Symbol.unnormalize(this)