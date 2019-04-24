package io.github.m

import org.objectweb.asm.*
import org.objectweb.asm.commons.*

/**
 * Jvm backend.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
object Backend {
    val internals: Map<String, (GeneratorAdapter) -> Unit> = listOf<java.lang.Class<*>>(
            Bool::class.java,
            Char.Definitions::class.java,
            Data.Definitions::class.java,
            Declaration.Definitions::class.java,
            Either.Definitions::class.java,
            Error.Definitions::class.java,
            File.Definitions::class.java,
            Generator.Definitions::class.java,
            Interpreter.Definitions::class.java,
            List::class.java,
            Nat.Definitions::class.java,
            Operation.Definitions::class.java,
            Pair::class.java,
            Process.Definitions::class.java,
            Stdio.Definitions::class.java,
            Symbol.Definitions::class.java
    ).flatMap {
        val type = Type.getType("L${it.name.replace('.', '/')};")
        it.fields.asSequence()
                .filter { field -> field.isAnnotationPresent(MField::class.java) }
                .map { field -> field.getAnnotation(MField::class.java).name to { ge: GeneratorAdapter -> ge.getStatic(type, field.name, Type.getType("Lio/github/m/Value;")) } }
                .toList()
    }.toMap()

    fun clazz(
            path: String,
            declarations: Sequence<Declaration>
    ): ByteArray = object : ClassWriter(COMPUTE_FRAMES) { }.run {
        val type = Type.getType("L$path;")
        visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, path, null, "java/lang/Object", null)

        declarations.forEach { write(it) }

        GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, Method.getMethod("void <clinit> ()"), null, null, this).apply {
            declarations.forEach { init(it) }

            returnValue()
            endMethod()
        }

        if (declarations.any { it is Declaration.Def && it.name == List.NIL }) {
            GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, Method.getMethod("void main (java.lang.String[])"), null, null, this).apply {
                loadArg(0)
                push(type)
                invokeStatic(Type.getType("Lio/github/m/Internals;"), Method.getMethod("void run (java.lang.String[], java.lang.Class)"))
                returnValue()
                endMethod()
            }
        }

        visitSource("${path.substringAfterLast('/')}.m", null)

        visitEnd()
        toByteArray()
    }

    fun ClassWriter.write(declaration: Declaration) = when (declaration) {
        is Declaration.Def -> write(declaration)
        is Declaration.Fn -> write(declaration)
        else -> TODO(declaration.javaClass.name)
    }

    private fun ClassWriter.write(declaration: Declaration.Def) {
        val field = visitField(
                Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                declaration.name.toString.normalize(),
                "Lio/github/m/Value;",
                null,
                null
        )

        val av = field.visitAnnotation("Lio/github/m/MField;", true)
        av.visit("name", declaration.name.toString)
        av.visitEnd()

        field.visitEnd()
    }

    private fun ClassWriter.write(declaration: Declaration.Fn) {
        val args = (0..declaration.closures.count()).joinToString(", ", "(", ")") { "io.github.m.Value" }
        val type = Method.getMethod("io.github.m.Value ${declaration.name.toString.normalize()} $args")
        GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC, type, null, null, this).apply {
            write(declaration._value)
            returnValue()
            endMethod()
        }
    }

    fun GeneratorAdapter.init(declaration: Declaration) = when (declaration) {
        is Declaration.Def -> init(declaration)
        is Declaration.Fn -> init(declaration)
        else -> TODO(declaration.javaClass.name)
    }

    private fun GeneratorAdapter.init(declaration: Declaration.Def) {
        internals[declaration.name.toString]?.invoke(this) ?: write(declaration._value)
        putStatic(Type.getType("L${declaration.path.toString};"), declaration.name.toString.normalize(), Type.getType("Lio/github/m/Value;"))
    }

    @Suppress("unused")
    private fun GeneratorAdapter.init(@Suppress("UNUSED_PARAMETER") declaration: Declaration.Fn) {

    }

    fun GeneratorAdapter.write(operation: Operation) = when (operation) {
        is Operation.LocalVariable -> write(operation)
        is Operation.GlobalVariable -> write(operation)
        is Operation.If -> write(operation)
        is Operation.Def -> write(operation)
        is Operation.Fn -> write(operation)
        is Operation.Symbol -> write(operation)
        is Operation.Apply -> write(operation)
        is Operation.LineNumber -> write(operation)
        is Operation.Nil -> write(operation)
        else -> TODO(operation.javaClass.name)
    }

    private fun GeneratorAdapter.write(operation: Operation.LocalVariable) {
        loadArg(operation.index.nat.toInt())
    }

    private fun GeneratorAdapter.write(operation: Operation.GlobalVariable) {
        val type = Type.getType("L${operation.path.toString};")
        getStatic(type, operation.name.toString.normalize(), Type.getType("Lio/github/m/Value;"))
    }

    private fun GeneratorAdapter.write(operation: Operation.If) {
        val endLabel = newLabel()
        val falseLabel = newLabel()

        write(operation.cond)

        invokeStatic(Type.getType("Lio/github/m/Internals;"), Method.getMethod("boolean toPrimitiveBool (io.github.m.Value)"))

        ifZCmp(GeneratorAdapter.EQ, falseLabel)

        write(operation.`true`)
        goTo(endLabel)

        mark(falseLabel)

        write(operation.`false`)

        mark(endLabel)
    }

    private fun GeneratorAdapter.write(operation: Operation.Def) {
        getStatic(Type.getType("L${operation.path.toString};"), operation.name.toString.normalize(), Type.getType("Lio/github/m/Value;"))
    }

    private fun GeneratorAdapter.write(operation: Operation.Fn) {
        operation.closures.forEach { write(it as Operation) }
        val closureTypes = (0 until operation.closures.count()).joinToString("", "", "") { "Lio/github/m/Value;" }
        visitInvokeDynamicInsn(
                "invoke",
                "($closureTypes)Lio/github/m/Value;",
                Handle(
                        Opcodes.H_INVOKESTATIC,
                        "java/lang/invoke/LambdaMetafactory",
                        "metafactory",
                        "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                        false
                ),
                Type.getType("(Lio/github/m/Value;)Lio/github/m/Value;"),
                Handle(
                        Opcodes.H_INVOKESTATIC,
                        operation.path.toString,
                        operation.name.toString.normalize(),
                        "(${closureTypes}Lio/github/m/Value;)Lio/github/m/Value;",
                        false
                ),
                Type.getType("(Lio/github/m/Value;)Lio/github/m/Value;")
        )
    }

    private fun GeneratorAdapter.write(operation: Operation.Symbol) {
        newInstance(Type.getType("Lio/github/m/Symbol;"))
        dup()
        push(operation.name.toString)
        invokeConstructor(
                Type.getType("Lio/github/m/Symbol;"),
                Method.getMethod("void <init> (java.lang.String)")
        )
    }

    private fun GeneratorAdapter.write(operation: Operation.Apply) {
        write(operation.fn)
        write(operation.arg)
        invokeInterface(
                Type.getType("Lio/github/m/Value;"),
                Method.getMethod("io.github.m.Value invoke (io.github.m.Value)")
        )
    }

    private fun GeneratorAdapter.write(operation: Operation.LineNumber) {
        visitLineNumber(operation.line.nat.toInt(), mark())
        write(operation.operation)
    }

    private fun GeneratorAdapter.write(@Suppress("UNUSED_PARAMETER") operation: Operation.Nil) {
        getStatic(Type.getType("Lio/github/m/Internals;"), "nil", Type.getType("Lio/github/m/Value;"))
    }
}