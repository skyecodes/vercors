package app.vercors.feature.config.data.repository

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.domain.AppConstants
import app.vercors.core.filesystem.*
import app.vercors.feature.config.domain.model.DataDirectoryError
import app.vercors.feature.config.domain.repository.DataDirectoryRepository
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.ContentCacheKind
import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.io.bytestring.toHexString
import kotlinx.io.files.Path
import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.progress.loading

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DataDirectoryRepositoryImpl(
    private val appPaths: AppPaths,
    private val cryptographyService: CryptographyService,
    @Named(AppConstants.APP_ID_KEY) private val appId: String,
) : DataDirectoryRepository {
    context(_: Raise<DataDirectoryError>)
    override suspend fun move(newPath: String, onProgress: (Progress) -> Unit) {
        val oldRoot = appPaths.root
        val newRoot = withAppFolderName(appId, Path(newPath))
        // Copying a tree into itself would corrupt it
        if (newRoot.toString() == oldRoot.toString()) return
        ensure(ensureRootReachable(newRoot)) { DataDirectoryError.Unreachable }

        val defaultRoot = defaultAppRoot(appId)
        val markerFile = defaultRoot.dataDirectoryMarkerFile()
        val onFileCopied = byteProgressReporter(oldRoot.sizeRecursively(), onProgress)
        copyDataDirectory(newRoot, markerFile, onFileCopied)
        writeAppRoot(defaultRoot, newRoot)
        onProgress(done())
        deleteOldDataDirectory(newRoot, markerFile)
    }

    private fun byteProgressReporter(totalBytes: Long, onProgress: (Progress) -> Unit): (Long) -> Unit {
        val total = totalBytes.coerceAtLeast(1)
        var doneBytes = 0L
        return { bytes ->
            doneBytes += bytes
            onProgress(loading((doneBytes.toDouble() / total).coerceIn(0.0, 1.0)))
        }
    }

    context(_: Raise<DataDirectoryError>)
    private suspend fun copyDataDirectory(newRoot: Path, markerFile: Path, onFileCopied: (Long) -> Unit) {
        val newAppPaths = AppPaths(newRoot)
        try {
            // Content cache first: everything below links against it
            if (appPaths.contentCacheRoot.exists()) {
                appPaths.contentCacheRoot.copyRecursively(newAppPaths.contentCacheRoot, onFileCopied = onFileCopied)
            }
            if (appPaths.gameDirs.exists()) copyGameDirs(newAppPaths, onFileCopied)
            // Skips the redirect marker: it isn't app data
            appPaths.root.copyRecursivelySkipping(
                target = newRoot,
                skip = setOf(appPaths.contentCacheRoot.toString(), appPaths.gameDirs.toString(), markerFile.toString()),
                onFileCopied = onFileCopied,
            )
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.error(e) { "Failed to copy data directory from ${appPaths.root} to $newRoot" }
            raise(DataDirectoryError.CopyFailed)
        }
    }

    // Re-linked against the new cache instead of copied, so shared files keep sharing one inode
    private suspend fun copyGameDirs(newAppPaths: AppPaths, onFileCopied: (Long) -> Unit) {
        val contentKindByDirName = mapOf(
            appPaths.modsDir("").name to ContentCacheKind.Mod,
            appPaths.resourcePacksDir("").name to ContentCacheKind.ResourcePack,
            appPaths.shaderPacksDir("").name to ContentCacheKind.ShaderPack,
        )
        for (instanceDir in appPaths.gameDirs.list()) {
            val newInstanceDir = Path(newAppPaths.gameDirs, instanceDir.name)
            for (child in instanceDir.list()) {
                val kind = contentKindByDirName[child.name]
                val newChild = Path(newInstanceDir, child.name)
                if (kind != null && child.isDirectory()) {
                    copyContentDir(child, newChild, kind, newAppPaths, onFileCopied)
                } else {
                    child.copyRecursively(newChild, onFileCopied = onFileCopied)
                }
            }
        }
    }

    // The old root may be the default root holding the marker just written: never delete it, and keep cleanup best-effort
    private suspend fun deleteOldDataDirectory(newRoot: Path, markerFile: Path) {
        try {
            appPaths.root.deleteRecursivelySkipping(setOf(markerFile.toString()))
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.warn(e) { "Failed to delete old data directory ${appPaths.root} after moving to $newRoot" }
        }
    }

    private suspend fun copyContentDir(
        oldDir: Path,
        newDir: Path,
        kind: ContentCacheKind,
        newAppPaths: AppPaths,
        onFileCopied: (Long) -> Unit,
    ) {
        newDir.createDirectories()
        for (file in oldDir.list()) {
            if (file.isDirectory()) {
                copyContentDir(file, Path(newDir, file.name), kind, newAppPaths, onFileCopied)
                continue
            }
            val newFile = Path(newDir, file.name)
            val sha1 = file.source { cryptographyService.sha1(it) }.toHexString()
            val newCacheObject = newAppPaths.contentCacheObjectFile(kind, sha1)
            val size = file.metadataOrNull()?.size ?: 0
            if (newCacheObject.exists() && newCacheObject.linkOrCopyTo(newFile)) {
                onFileCopied(size)
            } else {
                file.copyRecursively(newFile, onFileCopied = onFileCopied)
            }
        }
    }
}

private suspend fun Path.copyRecursivelySkipping(
    target: Path,
    skip: Set<String>,
    onFileCopied: (Long) -> Unit,
) {
    if (toString() in skip) return
    if (isDirectory()) {
        target.createDirectories()
        for (child in list()) {
            child.copyRecursivelySkipping(Path(target, child.name), skip, onFileCopied)
        }
    } else {
        onFileCopied(copyTo(target))
    }
}

// Best-effort: a live watcher may resurrect files mid-delete, so callers treat failures as non-fatal
private suspend fun Path.deleteRecursivelySkipping(skip: Set<String>) {
    if (toString() in skip) return
    if (isDirectory()) {
        for (child in list()) {
            child.deleteRecursivelySkipping(skip)
        }
        if (list().isEmpty()) delete()
    } else {
        delete()
    }
}
