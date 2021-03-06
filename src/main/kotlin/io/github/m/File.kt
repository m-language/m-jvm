package io.github.m

/**
 * M wrapper class for files.
 */
data class File(val value: java.io.File) : Value {
    constructor(name: String) : this(java.io.File(name))

    val isDirectory get() = value.isDirectory
    val name get() = value.name
    val nameWithoutExtension get() = value.nameWithoutExtension

    fun child(name: String) = File(java.io.File(value, name))
    fun childFiles() = value.listFiles().asSequence().map { File(it) }

    fun exists() = Bool.valueOf(value.exists())

    fun read() = run {
        val reader = value.bufferedReader()
        generateSequence { if (reader.ready()) reader.read() else null }
                .map { it.toChar() }
    }

    fun write(chars: Sequence<Char>) = run {
        value.parentFile.mkdirs()
        val writer = value.bufferedWriter()
        chars.forEach { writer.write(it.value.toInt()) }
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
        @MField(name="file.from-path")
        @JvmField
        val fromPath: Value = Value.Impl1("file.from-path") { path -> File((path as Symbol).value) }

        @MField(name = "file.name")
        @JvmField
        val name: Value = Value.Impl1("file.name") { file -> Symbol.toList((file as File).name) }

        @MField(name = "file.name-without-extension")
        @JvmField
        val nameWithoutExtension: Value = Value.Impl1("file.name-without-extension") { file -> Symbol.toList((file as File).nameWithoutExtension) }

        @MField(name = "file.child")
        @JvmField
        val child: Value = Value.Impl2("file.child") { file, name -> (file as File).child(Symbol.toString(name)) }

        @MField(name = "file.exists?")
        @JvmField
        val exists: Value = Value.Impl1("file.exists?") { file -> Process { (file as File).exists() } }

        @MField(name = "file.read")
        @JvmField
        val read: Value = Value.Impl1("file.read") { file -> Process { (file as File).read().map(Char::valueOf).list() } }

        @MField(name = "file.write")
        @JvmField
        val write: Value = Value.Impl2("file.write") { file, text -> Process { (file as File).write(List.from(text).asSequence().map { it as Char }); List.NIL } }

        @MField(name = "file.child-files")
        @JvmField
        val childFiles: Value = Value.Impl1("file.child-files") { file -> Process { (file as File).childFiles().list() } }

        @MField(name = "file.directory?")
        @JvmField
        val isDirectory: Value = Value.Impl1("file.directory?") { file -> Process { Bool.valueOf((file as File).isDirectory) } }

        @MField(name = "file.copy")
        @JvmField
        val copy: Value = Value.Impl2("file.copy") { file, dest -> Process { (file as File).copy(dest as File); List.NIL } }

        @MField(name = "mpm-root")
        @JvmField
        val mpmRoot: Value = System.getenv("MPM_ROOT")?.let { File(it) }
                ?: File(java.io.File.listRoots().first()).child("mpm-root")
    }
}