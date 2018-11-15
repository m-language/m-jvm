package io.github.m

import java.lang.reflect.Modifier

/**
 * Internal definitions used by the M runtime.
 */
@Suppress("unused")
object Internals {
    @JvmStatic
    fun toPrimitiveBool(value: Value) = value.asBool.value

    @JvmField
    val nil: Value = List.Nil

    @JvmStatic
    fun apply(function: Value, arg: Value) =
            try {
                function.asFunction(arg)
            } catch (e: Error) {
                throw e
            } catch (e: Throwable) {
                throw Error.Internal(e)
            }

    @JvmStatic
    fun import(clazz: Class<*>): Value = Data.Impl(
            Symbol(clazz.name),
            clazz.fields
                    .asSequence()
                    .filter { Modifier.isStatic(it.modifiers) }
                    .filter { it.type == Value::class.java }
                    .map { Symbol(it.name) to it.get(null) as Value }
                    .toMap()
    )
}
