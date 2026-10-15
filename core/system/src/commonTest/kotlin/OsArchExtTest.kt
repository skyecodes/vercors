package app.vercors.core.system

import kotlin.test.Test
import kotlin.test.assertEquals

class OsArchExtTest {
    @Test
    fun `maps arm64 machine names to Arm64`() {
        assertEquals(OsArch.Arm64, "aarch64".toOsArch())
        assertEquals(OsArch.Arm64, "arm64".toOsArch())
    }

    @Test
    fun `maps x86-64 machine names to X64`() {
        assertEquals(OsArch.X64, "amd64".toOsArch())
        assertEquals(OsArch.X64, "x86_64".toOsArch())
    }

    @Test
    fun `maps 32-bit machine names to X86`() {
        assertEquals(OsArch.X86, "x86".toOsArch())
        assertEquals(OsArch.X86, "i686".toOsArch())
    }
}
