package io.github.m

import io.kotlintest.*
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.properties.*
import io.kotlintest.specs.StringSpec

class BoolTest : StringSpec({
    "x <: Bool => Bool.is(x) == true" {
        assertAll(Gen.mBool) {
            Bool.`is`(it).shouldBeTrue()
        }
    }

    "Bool.is(x) <: boolean" {
        assertAll(Gen.value) {
            shouldNotThrowAny {
                Bool.`is`(it)
            }
        }
    }

    "Bool.is(x) => Bool.primitiveFrom(x) == Bool.from(x).value() <: boolean" {
        assertAll(Gen.value) {
            shouldNotThrowAny {
                if (Bool.`is`(it)) {
                    Bool.primitiveFrom(it) shouldBe Bool.from(it).value
                }
            }
        }
    }
})