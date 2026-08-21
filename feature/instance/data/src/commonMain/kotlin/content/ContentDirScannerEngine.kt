package app.vercors.feature.instance.data.content

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.domain.MutableProgressiveStateFlow
import app.vercors.core.filesystem.*
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.data.mods.extractIcon
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.ModProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.bytestring.toHexString
import kotlinx.io.files.Path
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.map
import opensavvy.state.progressive.successfulWithProgress

/**
 * Generic engine behind [app.vercors.feature.instance.data.mods.InstanceModScannerService] and its
 * resourcepack/shaderpack equivalents — scans a per-instance directory and keeps a purely in-memory, reactive
 * view (one [ContentEntry] per file) in sync with what's actually on disk. A disabled file is one with
 * `.disabled` appended to its filename; "enabled" is never persisted anywhere, it's always derived on the spot
 * from whether the current on-disk filename carries that suffix.
 *
 * Two caches back this, only one of which touches disk:
 * - an in-process `Path -> sha1` memo, so re-observing an instance within the same app run doesn't re-hash a file
 *   whose size hasn't changed. Cleared implicitly on restart.
 * - [metadataCache]: the actual parsed metadata (title/description/version/icon/providers), keyed by the file's
 *   sha1 and shared by every instance — a file installed identically in multiple instances is only ever parsed
 *   once, and this survives restarts (only the hash itself gets recomputed after one).
 *
 * Updates flow in two hops: a [FileWatcherService] subscription on the instance directory reacts to file
 * add/change/remove by resolving/removing the corresponding entry in the in-memory state; [activate] re-runs a
 * full [reconcile] on every fresh subscription (not just the first), since the watcher is best-effort — at least
 * on the JVM `WatchService` backend it silently fails to attach if the directory doesn't exist at that exact
 * moment, with no error and no retry.
 */
internal class ContentDirScannerEngine(
    private val instanceDir: (InstanceId) -> Path,
    private val extension: String,
    private val metadataCache: HashedJsonCache<ContentFileMetadata>,
    private val iconCacheDir: Path,
    private val parser: ContentFileParser,
    private val fileWatcherService: FileWatcherService,
    private val cryptographyService: CryptographyService,
    private val backgroundScope: CoroutineScope
) {
    private data class HashMemo(val size: Long, val sha1: String)

    private val statesLock = Mutex()
    private val states = mutableMapOf<InstanceId, MutableProgressiveStateFlow<Nothing, Map<String, ContentEntry>>>()
    private val watchStartedFor = mutableSetOf<InstanceId>()

    private val hashMemoLock = Mutex()
    private val hashMemo = mutableMapOf<Path, HashMemo>()

    /**
     * Guards every read-modify-write against [metadataCache]. Without it, [resolveEntry] (driven by the file
     * watcher, which fires as soon as a newly-downloaded file appears on disk) and [recordOrigin] (called right
     * after that same download completes) can race: both read the cache, [resolveEntry] writes back metadata with
     * no providers, and if that write lands after [recordOrigin]'s provider-populated write, it silently clobbers
     * the just-recorded provider.
     */
    private val metadataLock = Mutex()

    fun observe(instanceId: InstanceId): ProgressiveFlow<Nothing, List<ContentEntry>> {
        val state = stateFor(instanceId)
        return state.onStart { backgroundScope.launch { activate(instanceId, state) } }
            .map { outcome -> outcome.map { it.values.toList() } }
    }

    private fun stateFor(instanceId: InstanceId): MutableProgressiveStateFlow<Nothing, Map<String, ContentEntry>> =
        states.getOrPut(instanceId) { MutableProgressiveStateFlow() }

    private suspend fun activate(
        instanceId: InstanceId,
        state: MutableProgressiveStateFlow<Nothing, Map<String, ContentEntry>>
    ) {
        reconcile(instanceId, state)

        val watcherAlreadyStarted = statesLock.withLock { !watchStartedFor.add(instanceId) }
        if (watcherAlreadyStarted) return

        backgroundScope.launch {
            fileWatcherService.watchDirectory(instanceDir(instanceId)).collect { event ->
                handleFileEvent(instanceId, state, event)
            }
        }
    }

    /** Full (re)scan: resolves every content file currently on disk into the in-memory state. */
    private suspend fun reconcile(
        instanceId: InstanceId,
        state: MutableProgressiveStateFlow<Nothing, Map<String, ContentEntry>>
    ) {
        val dir = instanceDir(instanceId)
        if (!dir.exists()) {
            dir.createDirectories()
            state.update { emptyMap<String, ContentEntry>().successfulWithProgress() }
            return
        }
        val resolved = dir.list()
            .filter { isContentFile(it.name, extension) }
            .mapNotNull { resolveEntry(it) }
            .associateBy { it.fileName }
        state.update { resolved.successfulWithProgress() }
    }

    private suspend fun handleFileEvent(
        instanceId: InstanceId,
        state: MutableProgressiveStateFlow<Nothing, Map<String, ContentEntry>>,
        event: FileEvent
    ) {
        if (!isContentFile(event.path.name, extension)) return
        val logicalName = contentLogicalFileName(event.path.name)
        if (event.type == FileEvent.Type.DELETE) {
            // A DELETE also fires for the "before" half of an enable/disable rename — only really remove the
            // entry if neither the enabled nor the disabled variant exists on disk anymore.
            val stillPresent = existingFile(instanceId, logicalName)
            if (stillPresent != null) updateEntry(state, stillPresent)
            else state.update { it.map { map -> map - logicalName } }
        } else {
            updateEntry(state, event.path)
        }
    }

    private suspend fun existingFile(instanceId: InstanceId, logicalName: String): Path? {
        val dir = instanceDir(instanceId)
        val enabledPath = Path(dir, logicalName)
        if (enabledPath.exists()) return enabledPath
        val disabledPath = Path(dir, "$logicalName.disabled")
        if (disabledPath.exists()) return disabledPath
        return null
    }

    private suspend fun updateEntry(
        state: MutableProgressiveStateFlow<Nothing, Map<String, ContentEntry>>,
        filePath: Path
    ) {
        val entry = resolveEntry(filePath) ?: return
        state.update { it.map { map -> map + (entry.fileName to entry) } }
    }

    /** Hashes [filePath] (memoized within this app run) and resolves it into a fully-built [ContentEntry]. */
    private suspend fun resolveEntry(filePath: Path): ContentEntry? {
        val size = filePath.metadataOrNull()?.size ?: return null
        val sha1 = hashOf(filePath, size)
        val metadata = metadataLock.withLock { metadataCache.get(sha1) ?: parseAndCache(filePath, sha1) }
        return ContentEntry(
            fileName = contentLogicalFileName(filePath.name),
            title = metadata.title,
            description = metadata.description,
            version = metadata.version,
            loader = metadata.loader?.let { runCatching { ModLoaderType.valueOf(it) }.getOrNull() },
            fileSize = size,
            iconFilePath = metadata.iconExtension?.let { Path(iconCacheDir, "$sha1.$it").toString() },
            providers = metadata.providers.map { ModProvider(it.platformId, it.projectId, it.fileId) },
            enabled = isContentFileEnabled(filePath.name),
            lastModified = filePath.lastModifiedOrNull()
        )
    }

    /** Memoized by the *logical* path, so toggling the `.disabled` suffix never busts the memo. */
    private suspend fun hashOf(filePath: Path, size: Long): String {
        val key = canonicalPath(filePath)
        hashMemoLock.withLock { hashMemo[key] }?.takeIf { it.size == size }?.let { return it.sha1 }
        val sha1 = filePath.source { cryptographyService.sha1(it) }.toHexString()
        hashMemoLock.withLock { hashMemo[key] = HashMemo(size, sha1) }
        return sha1
    }

    private fun canonicalPath(filePath: Path): Path = Path(filePath.parent!!, contentLogicalFileName(filePath.name))

    private suspend fun parseAndCache(filePath: Path, sha1: String): ContentFileMetadata {
        val parsed = parser.parse(filePath)
        val iconExtension = parsed.iconEntryPath
            ?.let { extractIcon(filePath, it, iconCacheDir, sha1) }
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotEmpty() }
        val metadata = ContentFileMetadata(
            title = parsed.title,
            description = parsed.description,
            version = parsed.version,
            loader = parsed.loader?.name,
            iconExtension = iconExtension
        )
        metadataCache.put(sha1, metadata)
        return metadata
    }

    /**
     * Called right after a file is downloaded via the install flow, to record which platform/project/file it
     * came from — this is the only source of that information, since it can't be derived from the file's own
     * bytes. Upserts (by [platformId]) into the global entry's provider list rather than overwriting it wholesale,
     * since the exact same file may already carry a provider recorded from a different instance or platform. Also
     * refreshes the in-memory state so the file shows up immediately, without waiting for the watcher or the next
     * reconcile.
     */
    suspend fun recordOrigin(
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    ) {
        val filePath = Path(instanceDir(instanceId), fileName)
        val size = filePath.metadataOrNull()?.size ?: return
        val sha1 = hashOf(filePath, size)

        metadataLock.withLock {
            val existing = metadataCache.get(sha1) ?: parseAndCache(filePath, sha1)
            val provider = ContentProviderMetadata(platformId, projectId, fileId)
            val updatedProviders = existing.providers.filterNot { it.platformId == platformId } + provider
            metadataCache.put(sha1, existing.copy(providers = updatedProviders))
        }

        updateEntry(stateFor(instanceId), filePath)
    }

    /**
     * Enables/disables a file by renaming it (appending/removing the conventional `.disabled` suffix) and
     * immediately refreshing its entry — not relying on the watcher to eventually catch up.
     */
    suspend fun setEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean) {
        val dir = instanceDir(instanceId)
        val enabledPath = Path(dir, fileName)
        val disabledPath = Path(dir, "$fileName.disabled")
        val from = if (enabled) disabledPath else enabledPath
        val to = if (enabled) enabledPath else disabledPath
        if (!from.exists()) return
        from.moveTo(to)

        updateEntry(stateFor(instanceId), to)
    }

    /** Permanently deletes a file (whichever of the enabled/disabled variants is present) and drops its entry. */
    suspend fun removeFile(instanceId: InstanceId, fileName: String) {
        val target = existingFile(instanceId, fileName) ?: return
        target.delete()
        stateFor(instanceId).update { it.map { map -> map - fileName } }
    }
}
