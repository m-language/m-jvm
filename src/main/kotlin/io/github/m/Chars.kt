package io.github.m

/**
 * M char definitions.
 */
@Suppress("unused")
object Chars {
    @MField("char.=")
    @JvmField
    val eq: Value = Function { x, y -> Bool((x as Char).value == (y as Char).value) }

    @MField("char->nat")
    @JvmField
    @ExperimentalUnsignedTypes
    val toNat: Value = Function { x -> Nat((x as Char).value.toInt().toUInt()) }
}