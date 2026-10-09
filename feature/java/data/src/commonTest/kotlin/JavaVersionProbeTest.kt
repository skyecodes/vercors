package app.vercors.feature.java.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JavaVersionProbeTest {
    @Test
    fun `parses a modern java version string`() {
        assertEquals(17, "17.0.9".toJavaMajorVersion())
        assertEquals(21, "21.0.1".toJavaMajorVersion())
    }

    @Test
    fun `parses a legacy 1_x java version string`() {
        assertEquals(8, "1.8.0_392".toJavaMajorVersion())
    }

    @Test
    fun `returns null for a non-numeric version string`() {
        assertNull("unknown".toJavaMajorVersion())
    }

    @Test
    fun `parses real -XshowSettings-properties output into a probe result`() {
        // Real (trimmed) stderr output from `java -XshowSettings:properties -version` on OpenJDK 17.
        val output = """
            openjdk version "17.0.9" 2023-10-17
            Property settings:
                java.vendor = Eclipse Adoptium
                java.vendor.url = https://adoptium.net/
                java.version = 17.0.9
                os.arch = amd64
                os.name = Linux
        """.trimIndent()

        val result = parseJavaProbeOutput(output)

        assertEquals(17, result?.majorVersion)
        assertEquals("17.0.9", result?.versionString)
        assertEquals("Eclipse Adoptium", result?.vendor)
        assertEquals("amd64", result?.arch)
    }

    @Test
    fun `returns null when the output has no java_version property`() {
        assertNull(parseJavaProbeOutput("bash: not-java: command not found"))
    }
}
