package app.vercors.feature.instance.data.content

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.domain.MutableProgressiveStateFlow
import app.vercors.core.filesystem.*
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.data.mods.extractIcon
import app.vercors.feature.instance.domain.model.ImportError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.ModProvider
import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.raise
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
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

private val logger = KotlinLogging.logger {}

// activate() reconciles on every subscription: the JVM watcher silently fails to attach to a missing directory
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

    // Guards metadataCache read-modify-writes: resolveEntry and recordOrigin race right after a download
    private val metadataLock = Mutex()

    fun observe(instanceId: InstanceId): ProgressiveFlow<Nothing, List<ContentEntry>> {
        val state = stateFor(instanceId)
        return state.onStart { backgroundScope.launch { activate(instanceId, state) } }
            .map { outcome -> outcome.map { it.values.toList() } }
    }

    // For bulk writes outside the APIs below: never rely on the watcher alone
    suspend fun refresh(instanceId: InstanceId) = reconcile(instanceId, stateFor(instanceId))

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
            // A DELETE also fires for the old name of an enable/disable rename
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
            iconFilePath = metadata.iconExtension?.let { Path(iconCacheDir, sha1.take(2), "$sha1.$it").toString() },
            providers = metadata.providers.map { ModProvider(it.platformId, it.projectId, it.fileId) },
            enabled = isContentFileEnabled(filePath.name),
            lastModified = filePath.lastModifiedOrNull()
        )
    }

    // Memoized by logical path so toggling .disabled never busts the memo
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
            ?.let { extractIcon(filePath, it, Path(iconCacheDir, sha1.take(2)), sha1) }
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

    // Upserts by platformId: the same file may already carry a provider from another instance
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

    context(_: Raise<ImportError>)
    suspend fun importFile(instanceId: InstanceId, sourcePath: Path) {
        ensure(isContentFile(sourcePath.name, extension)) { ImportError.UnsupportedFileType }
        val targetPath = try {
            val dir = instanceDir(instanceId)
            if (!dir.exists()) dir.createDirectories()
            Path(dir, sourcePath.name).also { sourcePath.copyTo(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Failed to import $sourcePath into $instanceId" }
            raise(ImportError.CopyFailed)
        }
        updateEntry(stateFor(instanceId), targetPath)
    }

    suspend fun removeFile(instanceId: InstanceId, fileName: String) {
        val target = existingFile(instanceId, fileName) ?: return
        target.delete()
        stateFor(instanceId).update { it.map { map -> map - fileName } }
    }
}
