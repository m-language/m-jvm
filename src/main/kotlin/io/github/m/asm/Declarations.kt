@file:JvmName("Declarations")

package io.github.m.asm

import io.github.m.Runtime
import io.github.m.Value
import io.github.m.cast
import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter

val Value.asDeclaration get() = cast<Declaration>()

fun block(declarations: Iterable<Declaration>) = Declaration { declarations.forEach { it.generate(this) } }

fun block(vararg declaration: Declaration) = block(declaration.asIterable())

fun defDeclaration(name: String, main: Type) = Field(
        Access().asPublic().asStatic(),
        valueType,
        name,
        main
)

fun lambdaDeclaration(name: String, closures: List<String>, operation: Operation) = Method(
        Access().asPrivate().asStatic().asSynthetic(),
        name,
        emptyList(),
        valueType,
        closures.map { valueType } + valueType,
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
    val hasRun = Field(Access().asPrivate().asStatic(), Type.boolean, "hasRun", type)
    val run = Method(
            Access().asPublic().asFinal().asStatic(),
            "run",
            emptyList(),
            Type.void,
            emptyList(),
            emptySet(),
            `return`(Operation {
                val endLabel = newLabel()

                getStaticField(type, "hasRun", Type.boolean).generate(this)
                ifZCmp(GeneratorAdapter.NE, endLabel)

                pushBoolean(true).generate(this)
                setStaticField(type, "hasRun", Type.boolean).generate(this)

                operation.generate(this)

                mark(endLabel)
            })
    )
    return Class(
            Access().asPublic().asFinal(),
            type.qualifiedName(),
            emptyList(),
            Type.`object`,
            emptySet(),
            block(declaration, main, hasRun, run, ClassSource("${type.qualifiedName().name}.m"))
    )
}