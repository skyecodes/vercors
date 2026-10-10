@file:OptIn(ExperimentalStdlibApi::class)

package app.vercors.shared.data

import korlibs.compression.lzma.SevenZip
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readLongLe

private const val LZMA_PROPERTIES_SIZE = 5

internal class LzmaDecodingException(message: String) : Exception(message)

internal fun decodeLzma(input: Source, output: Sink, onBytesWritten: (Long) -> Unit = {}) {
    val decoder = SevenZip.LzmaDecoder()
    if (!decoder.setDecoderProperties(input.readByteArray(LZMA_PROPERTIES_SIZE))) {
        throw LzmaDecodingException("Invalid LZMA stream properties")
    }
    val outSize = input.readLongLe()
    if (!decoder.code(input.asLzmaInput(), output.asLzmaOutput(onBytesWritten), outSize)) {
        throw LzmaDecodingException("Corrupt LZMA data")
    }
}

private fun Source.asLzmaInput(): SevenZip.LzmaInputStream = object : SevenZip.LzmaInputStream {
    // readByte throws EOFException: the range decoder would otherwise decode a -1 end-of-stream as data
    override fun read(): Int = readByte().toInt() and 0xFF
    override fun read(bytes: ByteArray, offset: Int, size: Int): Int = readAtMostTo(bytes, offset, offset + size)
}

private fun Sink.asLzmaOutput(onBytesWritten: (Long) -> Unit): SevenZip.LzmaOutputStream =
    object : SevenZip.LzmaOutputStream {
        override fun flush() = this@asLzmaOutput.flush()

        override fun write8(value: Int) {
            writeByte(value.toByte())
            onBytesWritten(1)
        }

        override fun write(bytes: ByteArray, offset: Int, size: Int) {
            this@asLzmaOutput.write(bytes, offset, offset + size)
            onBytesWritten(size.toLong())
        }
    }
