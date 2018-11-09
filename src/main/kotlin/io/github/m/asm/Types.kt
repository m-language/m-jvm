@file:JvmName("Types")

package io.github.m.asm

import io.github.m.*

val valueType by lazy { Type.clazz(io.github.m.Value::class.java) }
val functionType by lazy { Type.clazz(io.github.m.Function::class.java) }

val Value.asQualifiedName get() = QualifiedName(asList.map { it.asString })
val Value.asType get() = Type.clazz(asQualifiedName)