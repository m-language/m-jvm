package io.github.m

import org.objectweb.asm.*
import org.objectweb.asm.commons.*

/**
 * Jvm backend.
 */
object Backend {
    val internals: Map<String, (GeneratorAdapter) -> Unit> = listOf<Class<*>>(
            Bool::class.java,
            Char::class.java,
            Data::class.java,
            Either::class.java,
            Error::class.java,
            List::class.java,
            Nat::class.java,
            Pair::class.java,
            Process::class.java,
            Stdio::class.java,
            Symbol::class.java,
            File.Definitions::class.java,
            Declaration.Definitions::class.java,
            Compiler.Definitions::class.java,
            Interpreter.Definitions::class.java,
            Operation.Definitions::class.java,
            HTTP.Definitions::class.java
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
                invokeStatic(
                        Type.getType("Lio/github/m/Cli;"),
                        Method.getMethod("void run (java.lang.String[], java.lang.Class)")
                )
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
                Symbol.normalize(Symbol.toString(declaration.name)),
                "Lio/github/m/Value;",
                null,
                null
        )

        val av = field.visitAnnotation("Lio/github/m/MField;", true)
        av.visit("name", Symbol.toString(declaration.name))
        av.visitEnd()

        field.visitEnd()
    }

    private fun ClassWriter.write(declaration: Declaration.Fn) {
        val args = (0..declaration.closures.count()).joinToString(", ", "(", ")") { "io.github.m.Value" }
        val type = Method.getMethod("io.github.m.Value ${Symbol.normalize(Symbol.toString(declaration.name))} $args")
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
        internals[Symbol.toString(declaration.name)]?.invoke(this) ?: write(declaration._value)
        putStatic(Type.getType("L${Symbol.toString(declaration.path)};"), Symbol.normalize(Symbol.toString(declaration.name)), Type.getType("Lio/github/m/Value;"))
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
        loadArg(operation.index.value)
    }

    private fun GeneratorAdapter.write(operation: Operation.GlobalVariable) {
        val type = Type.getType("L${Symbol.toString(operation.path)};")
        getStatic(type, Symbol.normalize(Symbol.toString(operation.name)), Type.getType("Lio/github/m/Value;"))
    }

    private fun GeneratorAdapter.write(operation: Operation.If) {
        val endLabel = newLabel()
        val falseLabel = newLabel()

        write(operation.cond)

        invokeStatic(
                Type.getType("Lio/github/m/Bool;"),
                Method.getMethod("boolean primitiveFrom (io.github.m.Value)")
        )

        ifZCmp(GeneratorAdapter.EQ, falseLabel)

        write(operation.`true`)
        goTo(endLabel)

        mark(falseLabel)

        write(operation.`false`)

        mark(endLabel)
    }

    private fun GeneratorAdapter.write(operation: Operation.Def) {
        getStatic(Type.getType("L${Symbol.toString(operation.path)};"), Symbol.normalize(Symbol.toString(operation.name)), Type.getType("Lio/github/m/Value;"))
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
                        Symbol.toString(operation.path),
                        Symbol.normalize(Symbol.toString(operation.name)),
                        "(${closureTypes}Lio/github/m/Value;)Lio/github/m/Value;",
                        false
                ),
                Type.getType("(Lio/github/m/Value;)Lio/github/m/Value;")
        )
    }

    private fun GeneratorAdapter.write(operation: Operation.Symbol) {
        push(Symbol.toString(operation.name))
        invokeStatic(
                Type.getType("Lio/github/m/Symbol;"),
                Method.getMethod("io.github.m.Symbol valueOf (java.lang.String)")
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
        visitLineNumber(operation.line.value, mark())
        write(operation.operation)
    }

    private fun GeneratorAdapter.write(@Suppress("UNUSED_PARAMETER") operation: Operation.Nil) {
        getStatic(Type.getType("Lio/github/m/List;"), "NIL", Type.getType("Lio/github/m/List;"))
    }
}