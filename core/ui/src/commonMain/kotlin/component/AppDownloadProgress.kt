package app.vercors.core.ui.component

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.vercors.core.domain.DownloadProgress
import app.vercors.core.domain.toHumanReadableSize
import app.vercors.core.ui.CoreRes
import app.vercors.core.ui.downloadSpeed
import app.vercors.core.ui.downloadedFiles
import app.vercors.core.ui.downloadedOfTotal
import app.vercors.core.ui.theme.label
import app.vercors.core.ui.theme.textStyles
import com.composeunstyled.theme.Theme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val SPEED_SAMPLE_INTERVAL = 500.milliseconds
private val SPEED_WINDOW = 3.seconds

@Composable
fun AppDownloadProgress(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    download: DownloadProgress?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AppProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
        if (download != null) AppDownloadProgressCaption(download)
    }
}

@Composable
private fun AppDownloadProgressCaption(download: DownloadProgress) {
    val speed = rememberDownloadSpeed(download.downloadedBytes)
    val downloaded = download.downloadedBytes.toHumanReadableSize()
    val bytesText = download.totalBytes
        ?.let { stringResource(CoreRes.string.downloadedOfTotal, downloaded, it.toHumanReadableSize()) }
        ?: downloaded
    val filesText = if (download.totalFiles > 1) {
        stringResource(CoreRes.string.downloadedFiles, download.completedFiles, download.totalFiles)
    } else null
    val style = Theme[textStyles][label]

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppText(
            text = listOfNotNull(bytesText, filesText).joinToString(" · "),
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (speed != null) {
            AppText(
                text = stringResource(CoreRes.string.downloadSpeed, speed.toHumanReadableSize()),
                style = style,
                maxLines = 1,
            )
        }
    }
}

// Sampled on a timer rather than per report, so the speed decays to zero when a download stalls
@Composable
private fun rememberDownloadSpeed(downloadedBytes: Long): Long? {
    val currentBytes by rememberUpdatedState(downloadedBytes)
    val (speed, setSpeed) = remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(Unit) {
        val samples = ArrayDeque<Pair<TimeMark, Long>>()
        while (true) {
            val bytes = currentBytes
            if (samples.lastOrNull()?.let { bytes < it.second } == true) samples.clear()
            samples.addLast(TimeSource.Monotonic.markNow() to bytes)
            while (samples.size > 2 && samples.first().first.elapsedNow() > SPEED_WINDOW) samples.removeFirst()
            val (oldestMark, oldestBytes) = samples.first()
            val elapsedMs = oldestMark.elapsedNow().inWholeMilliseconds
            setSpeed(if (samples.size >= 2 && elapsedMs > 0) (bytes - oldestBytes) * 1000 / elapsedMs else null)
            delay(SPEED_SAMPLE_INTERVAL)
        }
    }
    return speed
}
