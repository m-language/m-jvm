package io.github.m

import io.kotlintest.properties.Gen

val Gen.Companion.mBool
    get() = bool().map { Bool.valueOf(it) }

val Gen.Companion.mNat
    get() = choose(0, 4).map { Nat.valueOf(it) }

val Gen.Companion.mChar
    get() = from(arrayOf(Char.space, Char.tab, Char.carriageReturn, Char.linefeed))

val Gen.Companion.simple
    get() = oneOf(mBool, mNat, mChar)

val Gen.Companion.mEither
    get() = object : Gen<Either> {
        override fun constants() = simple.constants().flatMap { listOf(Either.Left(it), Either.Right(it)) }
        override fun random() = simple.random().mapIndexed { index, value ->
            if (index % 2 == 0) Either.Left(value) else Either.Right(value)
        }
    }

val Gen.Companion.value: Gen<Value>
    get() = oneOf(mBool, mNat, mChar, mEither)