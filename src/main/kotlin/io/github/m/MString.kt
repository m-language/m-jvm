@file:JvmName("MString")

package io.github.m

/**
 * Converts a Java string to an M list.
 */
val String.mString get(): MList = MList.valueOf(map(MChar.Companion::valueOf))

/**
 * Converts a M list to a java string.
 */
val MList.string get(): String = String(toList().map { Cast.toChar(it).value }.toCharArray())