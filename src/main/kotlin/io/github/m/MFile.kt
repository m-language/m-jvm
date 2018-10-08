package io.github.m

import java.io.File
import kotlin.coroutines.experimental.buildSequence

/**
 * M wrapper class for files.
 */
data class MFile(val file: File) : MData {
    override val type = Companion.type

    override fun get(key: MKeyword): MAny = when (key.value) {
        "read" -> MProcess {
            val bufferedReader = file.bufferedReader()
            val sequence = buildSequence<MAny> {
                while (bufferedReader.ready()) {
                    val char = bufferedReader.read().toChar()
                    yield(MChar(char))
                }
            }
            MList.valueOf(sequence.asIterable())
        }
        else -> MUndefined
    }

    companion object : MAny {
        /**
         * The type of all files.
         */
        override val type = MKeyword("file")
    }

    @Suppress("unused")
    object Definitions {
        @MField("file")
        @JvmField
        val file: MAny = MFunction { name -> MFile(File(Cast.toList(name).string).absoluteFile) }
    }
}