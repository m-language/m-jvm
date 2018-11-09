@file:JvmName("MString")

package io.github.m

typealias MString = MList

/**
 * Converts a Java string to an M list.
 */
val String.mString get(): MList = MList.valueOf(map(::MChar))

/**
 * Converts a M list to a java string.
 */
val MList.string get(): String = String(toList().map { it.asChar.value }.toCharArray())

val MAny.asString get() = asList.string