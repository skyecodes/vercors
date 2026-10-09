package app.vercors.feature.project.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class GameVersionExtTest {
    @Test
    fun `merges a bare version with its patches into a wildcard`() {
        assertEquals(
            listOf("26.1.x"),
            listOf("26.1.2", "26.1.1", "26.1").summarizeStableGameVersions()
        )
    }

    @Test
    fun `merges consecutive patches without a bare version into a range`() {
        assertEquals(
            listOf("1.16.3-1.16.5"),
            listOf("1.16.3", "1.16.4", "1.16.5").summarizeStableGameVersions()
        )
    }

    @Test
    fun `keeps a lone stable version as-is`() {
        assertEquals(
            listOf("1.19.4"),
            listOf("1.19.4").summarizeStableGameVersions()
        )
    }

    @Test
    fun `drops snapshot and pre-release versions`() {
        assertEquals(
            listOf("1.20"),
            listOf("1.20", "23w13a", "1.20-pre1", "1.16 Combat Test").summarizeStableGameVersions()
        )
    }

    @Test
    fun `sorts major minor groups newest first`() {
        assertEquals(
            listOf("1.16.5", "1.15.2", "1.14.4"),
            listOf("1.14.4", "1.16.5", "1.15.2").summarizeStableGameVersions()
        )
    }

    @Test
    fun `returns an empty list for empty input`() {
        assertEquals(emptyList(), emptyList<String>().summarizeStableGameVersions())
    }
}
