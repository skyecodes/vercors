package app.vercors.shared.data

import app.vercors.core.filesystem.createDirectories
import app.vercors.core.filesystem.sink
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class CurseForgeFingerprintTest {
    private val dir = Path("build/curseforge-fingerprint-test")

    private suspend fun fingerprintOf(name: String, bytes: ByteArray): Long {
        dir.createDirectories()
        val path = Path(dir, name)
        path.sink { it.write(bytes) }
        return path.curseForgeFingerprint()
    }

    @Test
    fun `fingerprints an empty file`() = runTest {
        assertEquals(1540447798, fingerprintOf("empty", byteArrayOf()))
    }

    @Test
    fun `fingerprints a file shorter than one block`() = runTest {
        assertEquals(1692487918, fingerprintOf("short", "ab".encodeToByteArray()))
    }

    @Test
    fun `fingerprints a file of whole blocks`() = runTest {
        assertEquals(1080400154, fingerprintOf("block", "abcdefgh".encodeToByteArray()))
    }

    @Test
    fun `ignores whitespace bytes`() = runTest {
        assertEquals(1540447798, fingerprintOf("whitespace", " \t\r\n  ".encodeToByteArray()))
        assertEquals(1080400154, fingerprintOf("spaced", "ab cd\r\nef\tgh ".encodeToByteArray()))
    }

    @Test
    fun `fingerprints binary content as unsigned`() = runTest {
        val bytes = intArrayOf(0x50, 0x4b, 3, 4, 32, 10, 0xff, 0x80, 9, 13, 1, 2, 3, 0x7f, 0xfe)
            .map { it.toByte() }.toByteArray()
        assertEquals(2486618471, fingerprintOf("binary", bytes))
    }
}
