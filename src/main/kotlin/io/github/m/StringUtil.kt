@file:JvmName("MString")

package io.github.m

@ExperimentalUnsignedTypes
val String.toList get(): List = List.valueOf(asSequence().map(::Char))

@ExperimentalUnsignedTypes
val Value.toString get() = String((this as List).toList().map { (it as Char).value }.toCharArray())