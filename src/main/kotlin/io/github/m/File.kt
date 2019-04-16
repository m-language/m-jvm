package io.github.m

/**
 * M wrapper class for files
 */
data class File(val value: java.io.File) : Value {
    constructor(name: String) : this(java.io.File(name))

    val isDirectory get() = value.isDirectory
    val name get() = value.name
    val nameWithoutExtension get() = value.nameWithoutExtension

    fun child(name: String) = File(java.io.File(value, name))
    fun childFiles() = value.listFiles().asSequence().map { File(it) }

    fun exists() = Bool(value.exists())

    fun read() = run {
        val reader = value.bufferedReader()
        generateSequence { if (reader.ready()) reader.read() else null }
                .map { it.toChar() }
    }

    fun write(chars: Sequence<Char>) = run {
        value.parentFile.mkdirs()
        val writer = value.bufferedWriter()
        chars.forEach { writer.write(it.char.toInt()) }
        writer.flush()
    }

    fun copy(out: File) = value.copyRecursively(out.value, overwrite = true)

    override fun invoke(arg: Value) = this

    override fun toString() = value.toString()

    /**
     * M file definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("file.local-file")
        @JvmField
        val localFile: Value = File(java.io.File("."))

        @MField("file.name")
        @JvmField
        val name: Value = Value { file -> (file as File).name.toList }

        @MField("file.name-without-extension")
        @JvmField
        val nameWithoutExtension: Value = Value { file -> (file as File).nameWithoutExtension.toList }

        @MField("file.child")
        @JvmField
        val child: Value = Value { file, name -> (file as File).child(name.toString) }

        @MField("file.exists?")
        @JvmField
        val exists: Value = Value { file -> Process { (file as File).exists() } }

        @MField("file.read")
        @JvmField
        val read: Value = Value { file -> Process { List.valueOf((file as File).read().map(::Char)) } }

        @MField("file.write")
        @JvmField
        val write: Value = Value { file, text -> Process { (file as File).write(List.from(text).asSequence().map { it as Char }); List.Nil } }

        @MField("file.child-files")
        @JvmField
        val childFiles: Value = Value { file -> Process { List.valueOf((file as File).childFiles()) } }

        @MField("file.directory?")
        @JvmField
        val isDirectory: Value = Value { file -> Process { Bool((file as File).isDirectory) } }

        @MField("file.copy")
        @JvmField
        val copy: Value = Value { file, dest -> Process { (file as File).copy(dest as File); List.Nil } }

        @MField("mpm-root")
        @JvmField
        val mpmRoot: Value = File(java.io.File.listRoots().first()).child("mpm-root")
    }
}