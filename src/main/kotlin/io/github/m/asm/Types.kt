@file:JvmName("Types")

package io.github.m.asm

import io.github.m.*

typealias MType = MList

val mAnyType by lazy { Type.clazz(MAny::class.java) }
val mFunctionType by lazy { Type.clazz(MFunction::class.java) }

val MAny.asQualifiedName get() = QualifiedName(asList.map { it.asString })
val MAny.asType get() = Type.clazz(asQualifiedName)