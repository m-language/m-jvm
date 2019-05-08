package io.github.m

import io.kotlintest.properties.*
import io.kotlintest.specs.StringSpec
import io.kotlintest.shouldBe

class NatTest : StringSpec({
    "0.invoke(x, y) == y" {
        assertAll(Gen.value, Gen.value) { a, b ->
            Nat.zero.invoke(a, b) shouldBe b
        }
    }

    "1.invoke(x, y) == x.invoke(y)" {
        assertAll(Gen.value, Gen.value) { a, b ->
            Nat.one.invoke(a, b).toString() shouldBe a.invoke(b).toString()
        }
    }

    "n.invoke(inc, 0) == n" {
        assertAll(Gen.mNat) {
            (it.invoke(Nat.inc, Nat.zero) as Nat).value shouldBe it.value
        }
    }
})