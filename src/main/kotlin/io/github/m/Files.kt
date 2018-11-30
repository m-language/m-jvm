package io.github.m

import java.nio.file.Files

/**
 * M file definitions.
 */
@ExperimentalUnsignedTypes
@Suppress("unused")
object Files {
    @MField("file.local-file")
    @JvmField
    val localFile: Value = File(java.io.File("."))

    @MField("file.child")
    @JvmField
    val child: Value = Function { file, name -> Process { File(java.io.File((file as File).value, name.toString)) } }

    @MField("file.child-files")
    @JvmField
    val childFiles: Value = Function { file -> Process { List.valueOf(Files.newDirectoryStream((file as File).value.toPath()).asSequence().map { File(it.toFile()) }) } }

    @MField("file.directory?")
    @JvmField
    val isDirectory: Value = Function { file -> Process { Bool((file as File).value.isDirectory) } }

    @MField("file.name")
    @JvmField
    val name: Value = Function { file -> Process { (file as File).value.name.toList } }

    @MField("file.parent")
    @JvmField
    val parent: Value = Function { file -> Process { File((file as File).value.parentFile) } }

    @MField("file.read")
    @JvmField
    val read: Value = Function { file ->
        Process {
            val bufferedReader = (file as File).value.bufferedReader()
            val sequence = generateSequence<Value> {
                if (bufferedReader.ready())
                    Char(bufferedReader.read().toChar())
                else
                    null
            }
            List.valueOf(sequence)
        }
    }
}