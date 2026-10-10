package app.vercors.shared.data

import app.vercors.core.domain.DownloadProgress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

class DownloadProgressTrackerTest {
    private val timeSource = TestTimeSource()
    private val reports = mutableListOf<DownloadProgress>()
    private val tracker = DownloadProgressTracker(
        totalBytes = 300,
        totalFiles = 3,
        onProgress = { reports += it },
        reportInterval = 100.milliseconds,
        timeSource = timeSource,
    )

    @Test
    fun `reports the first chunk immediately`() {
        tracker.addBytes(10)
        assertEquals(listOf(DownloadProgress(10, 300, 0, 3)), reports)
    }

    @Test
    fun `drops reports inside the throttle interval`() {
        tracker.addBytes(10)
        timeSource += 50.milliseconds
        tracker.addBytes(10)
        tracker.completeFile()
        assertEquals(1, reports.size)
    }

    @Test
    fun `reports the accumulated state once the interval elapsed`() {
        tracker.addBytes(10)
        timeSource += 50.milliseconds
        tracker.addBytes(20)
        timeSource += 60.milliseconds
        tracker.completeFile()
        assertEquals(DownloadProgress(30, 300, 1, 3), reports.last())
    }

    @Test
    fun `reportNow bypasses the throttle`() {
        tracker.addBytes(10)
        tracker.addBytes(290)
        tracker.reportNow()
        assertEquals(DownloadProgress(300, 300, 0, 3), reports.last())
        assertEquals(2, reports.size)
    }
}
