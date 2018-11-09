@file:JvmName("Declarations")

package io.github.m.asm

import io.github.m.MAny
import io.github.m.Runtime
import io.github.m.cast

val MAny.asDeclaration get() = cast<Declaration>()

fun block(declarations: Iterable<Declaration>) = Declaration { declarations.forEach { it.generate(this) } }

fun block(vararg declaration: Declaration) = block(declaration.asIterable())

fun defDeclaration(name: String, main: Type) = Field(
        Access().asPublic().asStatic(),
        mAnyType,
        name,
        main
)

fun lambdaDeclaration(name: String, closures: List<String>, operation: Operation) = Method(
        Access().asPrivate().asStatic().asSynthetic(),
        name,
        emptyList(),
        mAnyType,
        closures.map { mAnyType } + mAnyType,
        emptySet(),
        block(operation, `return`)
)

fun mainClass(
        type: Type,
        operation: Operation,
        declaration: Declaration
): Class {
    val runType = MethodType(
            "run",
            emptyList(),
            Type.void,
            listOf(Type.array(Type.string), Type.clazz(java.lang.Class::class.java)),
            emptySet()
    )
    val main = Method(
            Access().asPublic().asFinal().asStatic(),
            "main",
            emptyList(),
            Type.void,
            listOf(Type.array(Type.string)),
            emptySet(),
            block(
                    pushArg(0),
                    pushType(type),
                    invokeStatic(Type.clazz(Runtime::class.java), runType),
                    `return`
            )
    )
    val run = Method(
            Access().asPublic().asFinal().asStatic(),
            "run",
            emptyList(),
            Type.void,
            emptyList(),
            emptySet(),
            `return`(operation)
    )
    return Class(
            Access().asPublic().asFinal(),
            type.qualifiedName(),
            emptyList(),
            Type.`object`,
            emptySet(),
            block(declaration, main, run, ClassSource("${type.qualifiedName().name}.m"))
    )
}