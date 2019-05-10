package io.github.m

import io.kotlintest.matchers.collections.shouldBeOneOf
import io.kotlintest.properties.*
import io.kotlintest.specs.StringSpec
import io.kotlintest.shouldBe

class EitherTest : StringSpec({
    "x <: either => x.fold(a, b) == a | b" {
        assertAll(Gen.mEither, Gen.value, Gen.value) { x, a, b ->
            x.fold(a, b) shouldBeOneOf listOf(a, b)
        }
    }
})