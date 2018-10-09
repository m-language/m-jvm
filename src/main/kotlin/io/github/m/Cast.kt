package io.github.m

import kotlin.reflect.full.companionObjectInstance

/**
 * Functions for casting M values.
 */
@Suppress("unused", "NOTHING_TO_INLINE")
object Cast {
    @JvmStatic
    inline fun toBool(any: MAny) = cast<MBool>(any)

    @JvmStatic
    inline fun toNat(any: MAny) = cast<MNat>(any)

    @JvmStatic
    inline fun toInt(any: MAny) = cast<MInt>(any)

    @JvmStatic
    inline fun toReal(any: MAny) = cast<MReal>(any)

    @JvmStatic
    inline fun toChar(any: MAny) = cast<MChar>(any)

    @JvmStatic
    inline fun toList(any: MAny) = cast<MList>(any)

    @JvmStatic
    inline fun toSymbol(any: MAny) = cast<MSymbol>(any)

    @JvmStatic
    inline fun toFunction(any: MAny) = cast<MFunction>(any)

    @JvmStatic
    inline fun toProcess(any: MAny) = cast<MProcess>(any)

    @JvmStatic
    inline fun toData(any: MAny) = cast<MData>(any)

    @JvmStatic
    inline fun toFile(any: MAny) = cast<MFile>(any)

    @JvmStatic
    inline fun toPrimitiveBool(any: MAny) = toBool(any).value

    inline fun <reified T : MAny> cast(any: MAny) = try {
        any as T
    } catch (e: ClassCastException) {
        throw MError.Cast(any.type, (T::class.companionObjectInstance as MAny).type)
    }
}