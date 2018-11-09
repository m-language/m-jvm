@file:Suppress("NOTHING_TO_INLINE", "unused")

package io.github.m

import kotlin.reflect.full.companionObjectInstance

inline fun MAny.asData(type: MSymbol) = try {
    if (this.type != type)
        throw MError.Cast(this.type, type)
    this as MData
} catch (e: ClassCastException) {
    throw MError.Cast(this.type, type)
}

inline val MAny.asBool get() = cast<MBool>()

inline val MAny.asInt get() = cast<MInt>()

inline val MAny.asReal get() = cast<MReal>()

inline val MAny.asChar get() = cast<MChar>()

inline val MAny.asList get() = cast<MList>()

inline val MAny.asCons get() = cast<MList.Cons>()

inline val MAny.asNil get() = cast<MList.Nil>()

inline val MAny.asSymbol get() = cast<MSymbol>()

inline val MAny.asFunction get() = cast<MFunction>()

inline val MAny.asProcess get() = cast<MProcess>()

inline val MAny.asFile get() = cast<MFile>()

inline fun <reified T : MAny> MAny.cast() = try {
    this as T
} catch (e: ClassCastException) {
    throw MError.Cast(this.type, ((T::class.companionObjectInstance
            ?: T::class.objectInstance
            ?: throw Exception("${T::class.java} does not have a companion")) as MAny).type)
}

object Cast {
    @JvmStatic
    inline fun toPrimitiveBool(any: MAny) = any.asBool.value
}
