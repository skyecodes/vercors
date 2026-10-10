package app.vercors.shared.data

import kotlinx.io.Buffer
import kotlinx.io.EOFException
import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LzmaDecodingTest {
    private val plainText = "Hello LZMA! Hello LZMA! Hello LZMA! vercors\n"

    // `xz --format=lzma` output: unknown uncompressed size (all 0xFF) terminated by an end marker
    private val unknownSizeStream = byteArrayOf(
        93, 0, 0, -128, 0, -1, -1, -1, -1, -1, -1, -1, -1, 0, 36, 25, 73, -104, 111, 16, 15, 6, 62, -75, 19, 15,
        79, -77, 116, 7, 32, -64, 62, 89, -34, 121, 31, 118, 81, 105, -1, -1, -73, -122, 0, 0,
    )

    private val knownSizeStream = unknownSizeStream.copyOf().also { stream ->
        val size = plainText.length.toLong()
        for (i in 0 until Long.SIZE_BYTES) stream[5 + i] = (size shr (8 * i)).toByte()
    }

    private fun decode(stream: ByteArray): String {
        val output = Buffer()
        decodeLzma(Buffer().apply { write(stream) }, output)
        return output.readString()
    }

    @Test
    fun `decodes a stream with an end marker and unknown size`() {
        assertEquals(plainText, decode(unknownSizeStream))
    }

    @Test
    fun `decodes a stream with an explicit uncompressed size`() {
        assertEquals(plainText, decode(knownSizeStream))
    }

    @Test
    fun `fails on a truncated stream instead of decoding garbage`() {
        assertFailsWith<EOFException> { decode(knownSizeStream.copyOf(knownSizeStream.size - 12)) }
    }

    @Test
    fun `fails on invalid stream properties`() {
        val invalid = unknownSizeStream.copyOf().also { it[0] = -1 }
        assertFailsWith<LzmaDecodingException> { decode(invalid) }
    }
}
