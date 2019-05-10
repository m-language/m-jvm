package io.github.m

import io.kotlintest.*
import io.kotlintest.properties.*
import io.kotlintest.specs.StringSpec

class BoolTest : StringSpec({
    "x <: Bool => Bool.primitiveFrom(x) == Bool.from(x).value()" {
        assertAll(Gen.mBool) {
            shouldNotThrowAny {
                Bool.primitiveFrom(it) shouldBe Bool.from(it).value
            }
        }
    }
})