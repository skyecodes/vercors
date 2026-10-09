package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.ManualDownloadItem
import app.vercors.feature.instance.domain.usecase.RecordInstalledContentOriginUseCase
import app.vercors.feature.project.domain.model.ManualDownloadCandidate
import app.vercors.feature.project.domain.model.ManualDownloadError
import arrow.core.raise.recover
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Parallel: opening a browser tab per file is pointless if they're then awaited one at a time
@Inject
class RunManualDownloadsUseCase(
    private val awaitManualDownload: AwaitManualDownloadUseCase,
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase
) {
    suspend operator fun invoke(
        candidates: List<ManualDownloadCandidate>,
        onError: suspend (ManualDownloadError) -> Unit,
        onItemsChanged: suspend (List<ManualDownloadItem>) -> Unit
    ): Boolean {
        if (candidates.isEmpty()) return true

        val mutex = Mutex()
        var items = candidates.map { ManualDownloadItem(it.fileName, it.downloadUrl, downloaded = false) }
        onItemsChanged(items)

        val results = coroutineScope {
            candidates.map { candidate ->
                async {
                    val ok = awaitAndRecord(candidate, onError)
                    if (ok) {
                        mutex.withLock {
                            items = items.map {
                                if (it.fileName == candidate.fileName) it.copy(downloaded = true) else it
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

    private suspend fun awaitAndRecord(
        candidate: ManualDownloadCandidate,
        onError: suspend (ManualDownloadError) -> Unit
    ): Boolean = recover({
        awaitManualDownload(
            candidate.fileName,
            candidate.instanceId,
            candidate.projectType,
            candidate.size,
            candidate.sha1,
            candidate.md5
        )
        recordInstalledContentOrigin(
            candidate.contentKind,
            candidate.instanceId,
            candidate.fileName,
            candidate.platformId,
            candidate.providerProjectId,
            candidate.providerFileId
        )
        true
    }) { error: ManualDownloadError ->
        onError(error)
        false
    }
}
