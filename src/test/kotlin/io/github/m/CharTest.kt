package io.github.m

import io.kotlintest.properties.*
import io.kotlintest.specs.StringSpec
import io.kotlintest.shouldBe

class CharTest : StringSpec({
    "x <: int => x <: char" {
        assertAll(Gen.mNat) {
            Char.from(it).value.toInt() shouldBe it.value
        }
    }
})