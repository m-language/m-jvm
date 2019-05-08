package io.github.m

import io.kotlintest.properties.Gen

val Gen.Companion.mBool
    get() = Gen.bool().map { Bool.valueOf(it) }

val Gen.Companion.value
    get() = oneOf(mBool)