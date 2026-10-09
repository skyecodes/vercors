package app.vercors.feature.instance.ui.install

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.ManualDownloadItem
import app.vercors.feature.instance.domain.model.PendingManualDownload
import app.vercors.feature.instance.domain.usecase.RecordInstalledContentOriginUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceUseCase
import app.vercors.feature.instance.ui.message
import app.vercors.feature.project.domain.model.ManualDownloadError
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.usecase.AwaitManualDownloadUseCase
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import arrow.core.raise.recover
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val logger = KotlinLogging.logger {}

data class ManualDownloadCandidate(
    val fileName: String,
    val downloadUrl: String,
    val instanceId: InstanceId,
    val projectType: ProjectType,
    val size: Long?,
    val sha1: String?,
    val md5: String?,
    val platformId: String,
    val providerProjectId: String,
    val providerFileId: String,
)

private fun ProjectType.toContentKind(): ContentKind = when (this) {
    ProjectType.Mod -> ContentKind.Mod
    ProjectType.ResourcePack -> ContentKind.ResourcePack
    ProjectType.ShaderPack -> ContentKind.ShaderPack
    ProjectType.Modpack, ProjectType.DataPack, ProjectType.World ->
        error("Manual download is not supported for project type $this")
}

private fun ContentKind.toProjectType(): ProjectType = when (this) {
    ContentKind.Mod -> ProjectType.Mod
    ContentKind.ResourcePack -> ProjectType.ResourcePack
    ContentKind.ShaderPack -> ProjectType.ShaderPack
}

fun ManualDownloadCandidate.toPendingManualDownload(): PendingManualDownload = PendingManualDownload(
    fileName = fileName,
    downloadUrl = downloadUrl,
    size = size,
    sha1 = sha1,
    md5 = md5,
    contentKind = projectType.toContentKind(),
    platformId = platformId,
    providerProjectId = providerProjectId,
    providerFileId = providerFileId,
)

fun PendingManualDownload.toManualDownloadCandidate(instanceId: InstanceId): ManualDownloadCandidate =
    ManualDownloadCandidate(
        fileName = fileName,
        downloadUrl = downloadUrl,
        instanceId = instanceId,
        projectType = contentKind.toProjectType(),
        size = size,
        sha1 = sha1,
        md5 = md5,
        platformId = platformId,
        providerProjectId = providerProjectId,
        providerFileId = providerFileId,
    )

// Parallel: opening a browser tab per file is pointless if they're then awaited one at a time
suspend fun runManualDownloads(
    candidates: List<ManualDownloadCandidate>,
    awaitManualDownload: AwaitManualDownloadUseCase,
    recordInstalledContentOrigin: RecordInstalledContentOriginUseCase,
    snackbarService: SnackbarService,
    onItemsChanged: suspend (List<ManualDownloadItem>) -> Unit
): Boolean {
    if (candidates.isEmpty()) return true

    val mutex = Mutex()
    var items = candidates.map { ManualDownloadItem(it.fileName, it.downloadUrl, downloaded = false) }
    onItemsChanged(items)

    val results = coroutineScope {
        candidates.map { candidate ->
            async {
                val ok = recover({
                    awaitManualDownload(
                        candidate.fileName,
                        candidate.instanceId,
                        candidate.projectType,
                        candidate.size,
                        candidate.sha1,
                        candidate.md5
                    )
                    true
                }) { error: ManualDownloadError ->
                    snackbarService.error { error.message }
                    false
                }
                if (ok) {
                    recordInstalledContentOrigin(
                        candidate.projectType.toContentKind(),
                        candidate.instanceId,
                        candidate.fileName,
                        candidate.platformId,
                        candidate.providerProjectId,
                        candidate.providerFileId
                    )
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

// Best-effort: must never fail the surrounding install
suspend fun persistPendingManualDownloads(
    instanceId: InstanceId,
    candidates: List<ManualDownloadCandidate>,
    items: List<ManualDownloadItem>,
    updateInstance: UpdateInstanceUseCase,
) {
    val pending = if (items.isEmpty()) {
        emptyList()
    } else {
        val done = items.filter { it.downloaded }.map { it.fileName }.toSet()
        candidates.filterNot { it.fileName in done }.map { it.toPendingManualDownload() }
    }
    either { updateInstance(instanceId) { it.copy(pendingManualDownloads = pending) } }
        .onLeft { error -> logger.warn { "Failed to persist pending manual downloads for $instanceId: $error" } }
}
