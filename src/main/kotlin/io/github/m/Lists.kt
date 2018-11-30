package io.github.m

/**
 * M list definitions.
 */
@Suppress("unused")
object Lists {
    @MField("nil")
    @JvmField
    val nil: Value = List.Nil

    @MField("nil?")
    @JvmField
    val isNil: Value = Function { arg -> Bool(arg === List.Nil) }

    @MField("cons")
    @JvmField
    val cons: Value = Function { car, cdr -> List.Cons(car, cdr as List) }

    @MField("car")
    @JvmField
    val car: Value = Function { arg -> (arg as List.Cons).car }

    @MField("cdr")
    @JvmField
    val cdr: Value = Function { arg -> (arg as List.Cons).cdr }
}