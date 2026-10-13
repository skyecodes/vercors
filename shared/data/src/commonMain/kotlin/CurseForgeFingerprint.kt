package app.vercors.shared.data

import app.vercors.core.filesystem.source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.files.Path

private const val MURMUR2_M = 0x5bd1e995
private const val MURMUR2_SEED = 1
private const val READ_BUFFER_SIZE = 64 * 1024

// CurseForge's fingerprint: MurmurHash2 (seed 1) over the file minus tab, LF, CR and space bytes. The seed mixes in
// the filtered length, so the file is read twice
suspend fun Path.curseForgeFingerprint(): Long {
    val path = this
    return withContext(Dispatchers.IO) {
        var length = 0
        path.source { source -> source.forEachFingerprintByte { length++ } }
        val hasher = Murmur2(seed = MURMUR2_SEED xor length)
        path.source { source -> source.forEachFingerprintByte(hasher::update) }
        hasher.finish().toUInt().toLong()
    }
}

private inline fun Source.forEachFingerprintByte(block: (Byte) -> Unit) {
    val buffer = ByteArray(READ_BUFFER_SIZE)
    while (true) {
        val read = readAtMostTo(buffer)
        if (read == -1) break
        for (i in 0 until read) {
            val byte = buffer[i]
            if (!byte.isFingerprintWhitespace()) block(byte)
        }
    }
}

private fun Byte.isFingerprintWhitespace(): Boolean = this == 9.toByte() || this == 10.toByte() ||
        this == 13.toByte() || this == 32.toByte()

private class Murmur2(seed: Int) {
    private var hash = seed
    private var block = 0
    private var blockSize = 0

    fun update(byte: Byte) {
        block = block or ((byte.toInt() and 0xff) shl (8 * blockSize))
        if (++blockSize == 4) {
            var k = block * MURMUR2_M
            k = k xor (k ushr 24)
            k *= MURMUR2_M
            hash = (hash * MURMUR2_M) xor k
            block = 0
            blockSize = 0
        }
    }

    fun finish(): Int {
        var h = hash
        if (blockSize > 0) h = (h xor block) * MURMUR2_M
        h = h xor (h ushr 13)
        h *= MURMUR2_M
        return h xor (h ushr 15)
    }
}
