package io.github.m

/**
 * M wrapper class for files.
 */
data class File(val file: java.io.File) : Data {
    override val type get() = Companion.type

    override fun get(key: Symbol): Value = when (key.value) {
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
        "name" -> Process { file.name.m }
        "child" -> Function { name -> Process { File(java.io.File(file, name.asString)) } }
        "child-names" -> Process { List.valueOf(file.listFiles().asSequence().map { it.name.m }) }
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