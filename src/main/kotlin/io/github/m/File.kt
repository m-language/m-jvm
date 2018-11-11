package io.github.m

import java.nio.file.Files

/**
 * M wrapper class for files.
 */
data class File(val file: java.io.File) : Data {
    override val type get() = Companion.type

    override fun get(key: Symbol): Value = when (key.value) {
        "child" -> Function { name -> Process { File(java.io.File(file, name.asString)) } }
        "child-files" -> Process { List.valueOf(Files.newDirectoryStream(file.toPath()).asSequence().map { File(it.toFile()) }) }
        "directory?" -> Process { Bool.valueOf(file.isDirectory) }
        "name" -> Process { file.name.m }
        "parent" -> Process { File(file.parentFile) }
        "read" -> Process {
            val bufferedReader = file.bufferedReader()
            val sequence = generateSequence<Value> {
                if (bufferedReader.ready())
                    Char(bufferedReader.read().toChar())
                else
                    null
            }
            List.valueOf(sequence)
        }
        else -> noField(key)
    }

    companion object : Value {
        /**
         * The type of all files.
         */
        override val type = Symbol("file")
    }

    @Suppress("unused")
    object Definitions {
        @MField("local-file")
        @JvmField
        val localFile: Value = File(java.io.File("."))
    }
}