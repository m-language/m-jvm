package io.github.m

import io.kotlintest.properties.Gen

val Gen.Companion.mBool
    get() = Gen.bool().map { Bool.valueOf(it) }

val Gen.Companion.mNat
    get() = Gen.choose(0, Byte.MAX_VALUE.toInt()).map { Nat.valueOf(it) }

val Gen.Companion.value
    get() = oneOf(mBool, mNat)