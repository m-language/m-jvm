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

    fun read() = run {
        val reader = value.bufferedReader()
        generateSequence { if (reader.ready()) reader.read() else null }
                .map { it.toChar() }
    }

    override fun toString() = value.toString()

    /**
     * M file definitions.
     */
    @Suppress("unused")
    object Definitions {
        @MField("file.local-file")
        @JvmField
        val localFile: Value = File(java.io.File("."))

        @MField("file.child-files")
        @JvmField
        val childFiles: Value = Function { file -> Process { List.valueOf((file as File).childFiles()) } }

        @MField("file.directory?")
        @JvmField
        val isDirectory: Value = Function { file -> Process { Bool((file as File).isDirectory) } }

        @MField("file.read")
        @JvmField
        val read: Value = Function { file -> Process { List.valueOf((file as File).read().map(::Char)) } }

        @MField("file.name")
        @JvmField
        val name: Value = Function { file -> (file as File).name.toList }

        @MField("file.name-without-extension")
        @JvmField
        val nameWithoutExtension: Value = Function { file -> (file as File).nameWithoutExtension.toList }

        @MField("file.child")
        @JvmField
        val child: Value = Function { file, name -> (file as File).child(name.toString) }
    }
}