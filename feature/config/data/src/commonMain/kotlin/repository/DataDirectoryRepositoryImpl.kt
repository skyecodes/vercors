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
        val defaultRoot = defaultAppRoot(appId)
        // Defensive: the caller is expected to skip calling this when the (normalized) path is unchanged, but
        // copying a directory tree into itself below would corrupt it — never proceed regardless of the caller.
        if (newRoot.toString() == oldRoot.toString()) return
        ensure(ensureRootReachable(newRoot)) { DataDirectoryError.Unreachable }

        val newAppPaths = AppPaths(newRoot)
        val totalBytes = oldRoot.sizeRecursively().coerceAtLeast(1)
        var doneBytes = 0L
        val onFileCopied: (Long) -> Unit = { bytes ->
            doneBytes += bytes
            onProgress(loading((doneBytes.toDouble() / totalBytes).coerceIn(0.0, 1.0)))
        }

        try {
            // Content cache first — establishes the new cache before anything below tries to link against it.
            if (appPaths.contentCacheRoot.exists()) {
                appPaths.contentCacheRoot.copyRecursively(newAppPaths.contentCacheRoot, onFileCopied = onFileCopied)
            }

            // mods/resourcepacks/shaderpacks are re-linked against the new cache (see copyContentDir) instead of
            // byte-copied, so instances sharing a file keep sharing one inode at the new location too.
            if (appPaths.gameDirs.exists()) {
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

            // Everything else — root-level files, instance metadata, themes, logs, versions/libraries/assets,
            // java runtimes, the other (non-content) caches, tmp — a single generic copy skipping the two
            // subtrees already handled above (plus the redirect marker itself, if oldRoot happens to be the
            // OS-default location — it isn't app data, and copying it would leave a stale, confusing copy of
            // someone else's redirect sitting inside the new root).
            oldRoot.copyRecursivelySkipping(
                target = newRoot,
                skip = setOf(
                    appPaths.contentCacheRoot.toString(),
                    appPaths.gameDirs.toString(),
                    defaultRoot.dataDirectoryMarkerFile().toString(),
                ),
                onFileCopied = onFileCopied,
            )
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.error(e) { "Failed to copy data directory from $oldRoot to $newRoot" }
            raise(DataDirectoryError.CopyFailed)
        }

        writeAppRoot(defaultRoot, newRoot)
        onProgress(done())

        // oldRoot may be defaultRoot itself (the common case: moving away from the default location for the
        // first time) — the marker file just written above would live inside it. Never delete that: every other
        // FileStore/FileListStore still watching oldRoot's other files for the rest of this process's life can
        // also race a delete here and resurrect a default copy of what it watches (harmless clutter, unlike the
        // marker), which is why this whole cleanup stays best-effort/non-fatal.
        runCatching {
            oldRoot.deleteRecursivelySkipping(setOf(defaultRoot.dataDirectoryMarkerFile().toString()))
        }.onFailure {
            logger.warn(it) { "Failed to delete old data directory $oldRoot after moving to $newRoot" }
        }
    }

    /** Walks [oldDir]'s content files, re-linking each one against the new content cache when it matches a
     *  cached hash (already populated at [newAppPaths] by the time this runs) instead of copying its bytes —
     *  see the "Preserving the content-cache hardlinks" note in this feature's plan. A file with no cache
     *  match (not cache-eligible, or the cache was cleared/incomplete) falls back to a plain copy of the old
     *  file; old→new is never linked directly, only new cache → new instance file. */
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

/** Like [deleteRecursively], but never touches anything whose path is in [skip] — nor, consequently, any
 *  ancestor directory of a skipped path (it's left behind non-empty, on purpose). A file can still slip back
 *  into a directory between this listing it as empty and deleting it (e.g. a live `FileStoreImpl` watcher
 *  elsewhere in the process resurrecting a default copy of whatever it watches) — that's why every caller of
 *  this wraps the whole thing in `runCatching`; a leftover directory is an acceptable, non-fatal outcome,
 *  unlike deleting something in [skip]. */
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
