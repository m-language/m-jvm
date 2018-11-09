package io.github.m

import java.io.File

/**
 * M wrapper class for files.
 */
data class MFile(val file: File) : MData {
    override val type get() = Companion.type

    override fun get(key: MSymbol): MAny = when (key.value) {
        "read" -> MProcess {
            val bufferedReader = file.bufferedReader()
            val sequence = kotlin.sequences.generateSequence<MAny> {
                if (bufferedReader.ready())
                    MChar(bufferedReader.read().toChar())
                else
                    null
            }
            MList.valueOf(sequence.asIterable())
        }
        "name" -> MProcess { file.name.mString }
        else -> MUndefined
    }

    companion object : MAny {
        /**
         * The type of all files.
         */
        override val type = MSymbol("file")
    }

    @Suppress("unused")
    object Definitions {
        @MField("file")
        @JvmField
        val file: MAny = MFunction { name -> MFile(File(name.asString).absoluteFile) }
    }
}