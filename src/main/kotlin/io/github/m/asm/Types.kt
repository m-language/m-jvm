@file:JvmName("Types")

package io.github.m.asm

import io.github.m.*

val valueType = Type.clazz(io.github.m.Value::class.java)
val functionType = Type.clazz(io.github.m.Function::class.java)

val Value.asQualifiedName get() = QualifiedName.fromQualifiedString(asString)
val Value.asType get() = Type.clazz(asQualifiedName)