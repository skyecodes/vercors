package app.vercors.shared.data

import app.vercors.core.domain.DownloadProgress
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

private val DEFAULT_REPORT_INTERVAL = 100.milliseconds

// Throttled because up to MAX_PARALLEL_DOWNLOADS writers report every chunk
@OptIn(ExperimentalAtomicApi::class)
internal class DownloadProgressTracker(
    private val totalBytes: Long?,
    private val totalFiles: Int,
    private val onProgress: (DownloadProgress) -> Unit,
    private val reportInterval: Duration = DEFAULT_REPORT_INTERVAL,
    timeSource: TimeSource = TimeSource.Monotonic,
) {
    private val start = timeSource.markNow()
    private val downloadedBytes = AtomicLong(0)
    private val completedFiles = AtomicInt(0)
    private val lastReportNanos = AtomicLong(Long.MIN_VALUE)

    fun addBytes(count: Long) {
        downloadedBytes.addAndFetch(count)
        reportThrottled()
    }

    fun completeFile() {
        completedFiles.incrementAndFetch()
        reportThrottled()
    }

    fun reportNow() {
        lastReportNanos.store(start.elapsedNow().inWholeNanoseconds)
        onProgress(snapshot())
    }

    private fun reportThrottled() {
        val now = start.elapsedNow().inWholeNanoseconds
        val last = lastReportNanos.load()
        if (last != Long.MIN_VALUE && now - last < reportInterval.inWholeNanoseconds) return
        if (lastReportNanos.compareAndSet(last, now)) onProgress(snapshot())
    }

    private fun snapshot(): DownloadProgress = DownloadProgress(
        downloadedBytes = downloadedBytes.load(),
        totalBytes = totalBytes,
        completedFiles = completedFiles.load(),
        totalFiles = totalFiles,
    )
}
