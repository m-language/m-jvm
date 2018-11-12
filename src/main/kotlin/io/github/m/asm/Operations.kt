@file:JvmName("Operations")

package io.github.m.asm

import io.github.m.*
import jdk.internal.org.objectweb.asm.Handle
import jdk.internal.org.objectweb.asm.Opcodes
import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter
import jdk.internal.org.objectweb.asm.Type as AsmType

val Value.asOperation get() = cast<Operation>()

fun block(operations: Iterable<Operation>) = Operation { operations.forEach { it.generate(this) } }
fun block(vararg operations: Operation) = block(operations.asIterable())

fun cast(type: Type) = Operation { checkCast(type.asm) }

val dup = Operation { dup() }
val `return` = Operation { returnValue() }
val pop = Operation { pop() }

fun `return`(op: Operation) = block(op, `return`)

fun getStaticField(owner: Type, name: String, type: Type) = Operation { getStatic(owner.asm, name, type.asm) }
fun setStaticField(owner: Type, name: String, type: Type) = Operation { putStatic(owner.asm, name, type.asm) }

fun invokeConstructor(type: Type, methodType: MethodType) = Operation { invokeConstructor(type.asm, methodType.asm) }
fun invokeStatic(type: Type, methodType: MethodType) = Operation { invokeStatic(type.asm, methodType.asm) }

fun lineNumber(number: Int) = Operation { visitLineNumber(number, mark()) }

fun new(type: Type) = Operation { newInstance(type.asm) }

fun pushArg(index: Int) = Operation { loadArg(index) }
fun pushBoolean(boolean: Boolean) = Operation { push(boolean) }
fun pushString(string: String) = Operation { push(string) }
fun pushType(type: Type) = Operation { push(type.asm) }
val pushThis = Operation { loadThis() }

fun pushNew(type: Type, methodType: MethodType, args: Operation) = block(
        new(type),
        dup,
        args,
        invokeConstructor(type, methodType)
)

fun localVariableOperation(@Suppress("UNUSED_PARAMETER") name: String, index: Int) = pushArg(index)
fun globalVariableOperation(name: String, file: Type) = getStaticField(file, name, valueType)
fun reflectiveVariableOperation(name: String, file: Type) = globalVariableOperation(name, file)

val nilOperation = getStaticField(Type.clazz(io.github.m.List.Definitions::class.java), "nil", valueType)

fun ifOperation(cond: Operation, `true`: Operation, `false`: Operation): Operation = Operation {
    val endLabel = newLabel()
    val falseLabel = newLabel()

    cond.generate(this)

    invokeStatic(
            Type.clazz(io.github.m.Bool.Internal::class.java),
            MethodType("toPrimitiveBool", emptyList(), Type.boolean, listOf(valueType), emptySet())
    ).generate(this)

    ifZCmp(GeneratorAdapter.EQ, falseLabel)

    `true`.generate(this)
    goTo(endLabel)

    mark(falseLabel)

    `false`.generate(this)

    mark(endLabel)
}

fun defOperation(name: String, op: Operation, main: Type) = block(
        op,
        setStaticField(main, name, valueType),
        getStaticField(main, name, valueType)
)

fun lambdaOperation(
        main: Type,
        name: String,
        closures: List<Operation>
): Operation = Operation {
    val closureTypes = closures.map { valueType }
    closures.forEach { it.generate(this) }
    visitInvokeDynamicInsn(
            "invoke",
            closureTypes.joinToString("", "(", ")") { it.descriptor } + functionType.descriptor,
            Handle(
                    Opcodes.H_INVOKESTATIC,
                    "java/lang/invoke/LambdaMetafactory",
                    "metafactory",
                    "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"
            ),
            AsmType.getType("(${valueType.descriptor})${valueType.descriptor}"),
            Handle(
                    Opcodes.H_INVOKESTATIC,
                    main.qualifiedName().toPathString(),
                    name,
                    "(${closureTypes.joinToString("", "", "") { it.descriptor }}${valueType.descriptor})${valueType.descriptor}"
            ),
            AsmType.getType("(${valueType.descriptor})${valueType.descriptor}")
    )
}

fun symbolOperation(value: String) = pushNew(
        Type.clazz(Symbol::class.java),
        MethodType.constructor(listOf(Type.string), emptySet()),
        pushString(value)
)

fun includeOperation(name: String) = block(
        invokeStatic(
                Type.clazz(QualifiedName.fromQualifiedString(name)),
                MethodType("run", emptyList(), Type.void, emptyList(), emptySet())
        ),
        nilOperation
)

fun applyOperation(fn: Operation, arg: Operation) = block(
        fn,
        arg,
        invokeStatic(
                Type.clazz(io.github.m.Function.Internal::class.java),
                MethodType("apply", emptyList(), valueType, listOf(valueType, valueType), emptySet())
        )
)