package app.vercors.feature.project.data.repository

import app.vercors.core.filesystem.*
import app.vercors.core.system.SystemService
import app.vercors.feature.project.data.contentDir
import app.vercors.feature.project.data.toContentCacheKind
import app.vercors.feature.project.domain.model.ManualDownloadError
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.repository.ManualDownloadRepository
import app.vercors.shared.data.*
import arrow.core.raise.Raise
import arrow.core.raise.context.either
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.io.files.Path
import kotlin.time.Duration.Companion.milliseconds

private val STABLE_POLL_INTERVAL = 500.milliseconds

private const val STABLE_POLL_THRESHOLD = 3

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ManualDownloadRepositoryImpl(
    private val appPaths: AppPaths,
    private val fileValidationService: FileValidationService,
    private val systemService: SystemService,
    private val fileWatcherService: FileWatcherService,
) : ManualDownloadRepository {
    // An unchanged file from a previous pack version, or one another instance already downloaded manually
    override suspend fun installManualDownloadLocally(
        fileName: String,
        instanceId: String,
        type: ProjectType,
        size: Long?,
        sha1: String?,
        md5: String?
    ): Boolean = with(fileValidationService) {
        val request = FileValidationRequest.Url(
            url = "",
            path = Path(appPaths.contentDir(type, instanceId), fileName),
            size = size,
            sha1 = sha1,
            md5 = md5,
            cacheKind = type.toContentCacheKind()
        )
        if (either { request.validate() }.isRight()) {
            request.populateCache()
            return true
        }
        val served = either { request.downloadAndValidate() }.isRight()
        if (!served && request.path.exists()) request.path.delete()
        served
    }

    context(_: Raise<ManualDownloadError>)
    override suspend fun awaitManualDownload(
        fileName: String,
        instanceId: String,
        type: ProjectType,
        size: Long?,
        sha1: String?,
        md5: String?
    ) {
        val sourcePath = awaitStableDownloadInDownloadsFolder(fileName, size)
        val targetPath = Path(appPaths.contentDir(type, instanceId), fileName)
        try {
            // The target may be a hardlink into the content cache: copying over it would rewrite the cached object
            if (targetPath.exists()) targetPath.delete()
            sourcePath.copyTo(targetPath)
            sourcePath.delete()
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            raise(ManualDownloadError.DownloadFailed)
        }
        recover({
            with(fileValidationService) {
                val request = FileValidationRequest.Url(
                    url = "",
                    path = targetPath,
                    size = size,
                    sha1 = sha1,
                    md5 = md5,
                    cacheKind = type.toContentCacheKind()
                )
                request.validate()
                request.populateCache()
            }
        }) { _: FileValidationFailure ->
            if (targetPath.exists()) targetPath.deleteRecursively()
            raise(ManualDownloadError.IntegrityMismatch)
        }
    }

    context(_: Raise<ManualDownloadError>)
    private suspend fun awaitStableDownloadInDownloadsFolder(fileName: String, size: Long?): Path {
        val downloadsDir = Path(ensureNotNull(systemService.downloadsDir()) { ManualDownloadError.DownloadFailed })
        val sourcePath = Path(downloadsDir, fileName)
        if (!sourcePath.exists()) {
            fileWatcherService.watchDirectory(downloadsDir)
                .first { it.type == FileEvent.Type.CREATE && it.path.name == fileName }
        }
        // Wait for the browser to finish writing before moving/validating
        awaitDownloadStable(sourcePath, size)
        return sourcePath
    }

    context(_: Raise<ManualDownloadError>)
    override suspend fun awaitManualWorldDownload(
        fileName: String,
        instanceId: String,
        size: Long?,
        sha1: String?,
        md5: String?
    ) {
        val sourcePath = awaitStableDownloadInDownloadsFolder(fileName, size)
        recover({
            with(fileValidationService) {
                FileValidationRequest.Url(url = "", path = sourcePath, size = size, sha1 = sha1, md5 = md5).validate()
            }
        }) { _: FileValidationFailure ->
            if (sourcePath.exists()) sourcePath.deleteRecursively()
            raise(ManualDownloadError.IntegrityMismatch)
        }
        try {
            appPaths.extractWorldZip(sourcePath, instanceId, fileName)
            sourcePath.delete()
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            raise(ManualDownloadError.DownloadFailed)
        }
    }

    // No cross-platform "still being written" signal (no mtime on mingw), so poll the size
    private suspend fun awaitDownloadStable(path: Path, expectedSize: Long?) {
        var lastSize: Long? = null
        var stableCount = 0
        while (true) {
            val currentSize = path.metadataOrNull()?.size
            when {
                currentSize != null && currentSize == expectedSize -> return
                currentSize != null && currentSize == lastSize -> {
                    stableCount++
                    if (stableCount >= STABLE_POLL_THRESHOLD) return
                }

                else -> {
                    lastSize = currentSize
                    stableCount = 0
                }
            }
            delay(STABLE_POLL_INTERVAL)
        }
    }
}
