package app.vercors.feature.project.ui.install

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.model.ManualDownloadError
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.usecase.AwaitManualDownloadUseCase
import app.vercors.feature.project.ui.message
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.error
import arrow.core.raise.recover
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class ManualDownloadStatus { Waiting, Downloaded }

@Immutable
data class ManualDownloadItemView(
    val fileName: String,
    val downloadUrl: String,
    val status: ManualDownloadStatus,
)

internal data class ManualDownloadCandidate(
    val fileName: String,
    val downloadUrl: String,
    val instanceId: InstanceId,
    val projectType: ProjectType,
    val recordOrigin: suspend () -> Unit
)

/**
 * Runs [candidates]' manual downloads in parallel — opening one browser tab per file is pointless if we then only
 * watch for them one at a time — reporting live per-item status via [onItemsChanged] as each one is detected and
 * moved into place. Returns `true` only once every candidate has succeeded; a single failure still lets the others
 * finish before reporting overall failure, since there's no reason to abandon downloads already in flight.
 */
internal suspend fun runManualDownloads(
    candidates: List<ManualDownloadCandidate>,
    awaitManualDownload: AwaitManualDownloadUseCase,
    snackbarService: SnackbarService,
    onItemsChanged: (List<ManualDownloadItemView>) -> Unit
): Boolean {
    if (candidates.isEmpty()) return true

    val mutex = Mutex()
    var items = candidates.map { ManualDownloadItemView(it.fileName, it.downloadUrl, ManualDownloadStatus.Waiting) }
    onItemsChanged(items)

    val results = coroutineScope {
        candidates.map { candidate ->
            async {
                val ok = recover({
                    awaitManualDownload(candidate.fileName, candidate.instanceId, candidate.projectType)
                    true
                }) { error: ManualDownloadError ->
                    snackbarService.error { error.message }
                    false
                }
                if (ok) {
                    candidate.recordOrigin()
                    mutex.withLock {
                        items = items.map {
                            if (it.fileName == candidate.fileName) it.copy(status = ManualDownloadStatus.Downloaded) else it
                        }
                        onItemsChanged(items)
                    }
                }
                ok
            }
        }.awaitAll()
    }

    onItemsChanged(emptyList())
    return results.all { it }
}
