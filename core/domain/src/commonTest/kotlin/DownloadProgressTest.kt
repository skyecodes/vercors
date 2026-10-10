package app.vercors.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class DownloadProgressTest {
    @Test
    fun `uses the byte ratio when the total size is known`() {
        assertEquals(0.25, DownloadProgress(25, 100, completedFiles = 3, totalFiles = 4).normalized)
    }

    @Test
    fun `falls back to the file ratio when the total size is unknown`() {
        assertEquals(0.75, DownloadProgress(25, null, completedFiles = 3, totalFiles = 4).normalized)
    }

    @Test
    fun `never exceeds one when more bytes arrive than announced`() {
        assertEquals(1.0, DownloadProgress(150, 100, completedFiles = 1, totalFiles = 1).normalized)
    }

    @Test
    fun `is zero when there is nothing to download`() {
        assertEquals(0.0, DownloadProgress(0, 0, completedFiles = 0, totalFiles = 0).normalized)
    }
}
