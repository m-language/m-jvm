package io.github.m

import io.kotlintest.properties.Gen

val Gen.Companion.mBool
    get() = bool().map { Bool.valueOf(it) }

val Gen.Companion.mNat
    get() = choose(0, 4).map { Nat.valueOf(it) }

val Gen.Companion.mChar
    get() = from(arrayOf(Char.space, Char.tab, Char.carriageReturn, Char.linefeed))

val Gen.Companion.value
    get() = oneOf(mBool, mNat, mChar)