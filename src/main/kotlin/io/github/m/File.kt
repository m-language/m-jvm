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
            List.valueOf(sequence.asIterable())
        }
        "name" -> Process { file.name.m }
        else -> Undefined
    }

    companion object : Value {
        /**
         * The type of all files.
         */
        override val type = Symbol("file")
    }

    @Suppress("unused")
    object Definitions {
        @MField("file")
        @JvmField
        val file: Value = Function { name -> File(java.io.File(name.asString).absoluteFile) }
    }
}