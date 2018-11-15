@file:Suppress("NOTHING_TO_INLINE", "unused")

package io.github.m

import kotlin.reflect.full.companionObjectInstance

inline fun Value.asData(type: Symbol) = try {
    if (this.type != type)
        throw Error.Cast(this.type, type)
    this as Data
} catch (e: ClassCastException) {
    throw Error.Cast(this.type, type)
}

inline val Value.asBool get() = cast<Bool>()

inline val Value.asChar get() = cast<Char>()

inline val Value.asFile get() = cast<File>()

inline val Value.asFunction get() = cast<Function>()

inline val Value.asInt get() = cast<Int>()

inline val Value.asNat get() = cast<Nat>()

inline val Value.asList get() = cast<List>()

inline val Value.asProcess get() = cast<Process>()

inline val Value.asCons get() = cast<List.Cons>()

inline val Value.asNil get() = cast<List.Nil>()

inline val Value.asReal get() = cast<Real>()

inline val Value.asSymbol get() = cast<Symbol>()

inline val Value.asOperation get() = cast<Operation>()

inline val Value.asDeclaration get() = cast<Declaration>()

inline fun <reified T : Value> Value.cast() = try {
    this as T
} catch (e: ClassCastException) {
    throw Error.Cast(this.type, ((T::class.companionObjectInstance
            ?: T::class.objectInstance
            ?: throw Exception("${T::class.java} does not have a companion")) as Value).type)
}