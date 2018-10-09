@file:JvmName("Operations")

package io.github.m.asm

import io.github.m.*
import jdk.internal.org.objectweb.asm.Handle
import jdk.internal.org.objectweb.asm.Opcodes
import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter

fun block(operations: Iterable<Operation>) = Operation { operations.forEach { it.generate(this) } }
fun block(vararg operations: Operation) = block(operations.asIterable())

fun cast(type: Type) = Operation { checkCast(type.asm) }

val dup = Operation { dup() }
val `return` = Operation { returnValue() }

fun getStaticField(owner: Type, name: String, type: Type) = Operation { getStatic(owner.asm, name, type.asm) }
fun setStaticField(owner: Type, name: String, type: Type) = Operation { putStatic(owner.asm, name, type.asm) }

fun invokeConstructor(type: Type, methodType: MethodType) = Operation { invokeConstructor(type.asm, methodType.asm) }
fun invokeStatic(type: Type, methodType: MethodType) = Operation { invokeStatic(type.asm, methodType.asm) }

fun lineNumber(number: Int) = Operation { visitLineNumber(number, mark()) }

fun new(type: Type) = Operation { newInstance(type.asm) }

fun pushArg(index: Int) = Operation { loadArg(index) }
fun pushString(string: String) = Operation { push(string) }
fun pushType(type: Type) = Operation { push(type.asm) }
val pushThis = Operation { loadThis() }

fun pushNew(type: Type, methodType: MethodType, args: List<Operation>) = block(
        new(type),
        dup,
        block(args),
        invokeConstructor(type, methodType)
)

fun pushMGlobal(type: Type, name: String) = getStaticField(type, name, Gen.mAnyType)
fun pushMLocal(index: Int) = pushArg(index)

val pushMNil = getStaticField(Type.clazz(MList.Definitions::class.java), "nil", Gen.mAnyType)

fun pushMSymbol(value: String) = pushNew(
        Type.clazz(MSymbol::class.java),
        MethodType.constructor(listOf(Type.string), emptySet()),
        listOf(pushString(value))
)

fun initDef(name: String, op: Operation, main: Type) = block(
        op,
        setStaticField(main, name, Gen.mAnyType)
)

fun `if`(condition: Operation, ifTrue: Operation, ifFalse: Operation): Operation = Operation {
    val endLabel = newLabel()
    val falseLabel = newLabel()

    condition.generate(this)

    invokeStatic(
            Type.clazz(Cast::class.java),
            MethodType("toPrimitiveBool", emptyList(), Type.boolean, listOf(Gen.mAnyType), emptySet())
    ).generate(this)

    ifZCmp(GeneratorAdapter.EQ, falseLabel)

    ifTrue.generate(this)
    goTo(endLabel)

    mark(falseLabel)

    ifFalse.generate(this)

    mark(endLabel)
}

fun invoke(fn: Operation, arg: Operation) = block(
        fn,
        arg,
        invokeStatic(
                Type.clazz(MFunction.Internal::class.java),
                MethodType("apply", emptyList(), Gen.mAnyType, listOf(Gen.mAnyType, Gen.mAnyType), emptySet())
        )
)

fun lambdaConstructor(
        main: Type,
        name: String,
        closures: List<Operation>
): Operation = Operation {
    val closureTypes = closures.map { Gen.mAnyType }
    closures.forEach { it.generate(this) }
    visitInvokeDynamicInsn(
            "invoke",
            closureTypes.joinToString("", "(", ")") { it.descriptor } + Gen.mFunctionType.descriptor,
            Handle(
                    Opcodes.H_INVOKESTATIC,
                    "java/lang/invoke/LambdaMetafactory",
                    "metafactory",
                    "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"
            ),
            jdk.internal.org.objectweb.asm.Type.getType("(${Gen.mAnyType.descriptor})${Gen.mAnyType.descriptor}"),
            Handle(
                    Opcodes.H_INVOKESTATIC,
                    main.toQualifiedName().toPathString(),
                    name,
                    "(${closureTypes.joinToString("", "", "") { it.descriptor }}${Gen.mAnyType.descriptor})${Gen.mAnyType.descriptor}"
            ),
            jdk.internal.org.objectweb.asm.Type.getType("(${Gen.mAnyType.descriptor})${Gen.mAnyType.descriptor}")
    )
}