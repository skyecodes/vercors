package app.vercors.feature.instance.data.mods

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.domain.MutableProgressiveStateFlow
import app.vercors.core.filesystem.*
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstalledMod
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.ModProvider
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.bytestring.toHexString
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.map
import opensavvy.state.progressive.successfulWithProgress

/**
 * Scans an instance's `mods/` folder and keeps a purely in-memory, reactive view (one [InstalledMod] per jar) in
 * sync with what's actually on disk. A disabled mod is a jar with `.disabled` appended to its filename (the same
 * convention other launchers use); "enabled" is never persisted anywhere, it's always derived on the spot from
 * whether the current on-disk filename carries that suffix.
 *
 * Two caches back this, only one of which touches disk:
 * - [hashMemo]: an in-process `Path -> sha1` memo, so re-observing an instance within the same app run doesn't
 *   re-hash a jar whose size hasn't changed. Cleared implicitly on restart.
 * - [globalModMetadataCache]: the actual parsed metadata (title/description/version/icon/providers), keyed by the
 *   jar's sha1 and shared by every instance — a jar installed identically in multiple instances is only ever
 *   parsed once, and this survives restarts (only the hash itself gets recomputed after one).
 *
 * Updates flow in two hops: a [FileWatcherService] subscription on the *mods* directory reacts to jar
 * add/change/remove by resolving/removing the corresponding entry in the in-memory state; [activate] re-runs a
 * full [reconcile] on every fresh subscription (not just the first), since the watcher is best-effort — at least
 * on the JVM `WatchService` backend it silently fails to attach if the directory doesn't exist at that exact
 * moment, with no error and no retry.
 */
@SingleIn(AppScope::class)
@Inject
class InstanceModScannerService(
    private val appPaths: AppPaths,
    private val fileWatcherService: FileWatcherService,
    private val hashedJsonCacheFactory: HashedJsonCacheFactory,
    private val cryptographyService: CryptographyService,
    private val backgroundScope: CoroutineScope,
    private val json: Json
) {
    private data class HashMemo(val size: Long, val sha1: String)

    private val statesLock = Mutex()
    private val states = mutableMapOf<InstanceId, MutableProgressiveStateFlow<Nothing, Map<String, InstalledMod>>>()
    private val watchStartedFor = mutableSetOf<InstanceId>()

    private val hashMemoLock = Mutex()
    private val hashMemo = mutableMapOf<Path, HashMemo>()

    private val globalModMetadataCache: HashedJsonCache<GlobalModMetadata> by lazy {
        hashedJsonCacheFactory(appPaths.modMetadataCache, GlobalModMetadata.serializer())
    }

    fun observe(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledMod>> {
        val state = stateFor(instanceId)
        return state.onStart { backgroundScope.launch { activate(instanceId, state) } }
            .map { outcome -> outcome.map { it.values.toList() } }
    }

    private fun stateFor(instanceId: InstanceId): MutableProgressiveStateFlow<Nothing, Map<String, InstalledMod>> =
        states.getOrPut(instanceId) { MutableProgressiveStateFlow() }

    /**
     * Runs on every fresh subscription (not just the first), unlike the watcher below. The underlying JVM
     * `WatchService` (via kfswatch) silently drops registration if the target directory doesn't exist at the
     * exact moment it's registered, with no retry — so a one-shot [reconcile] would permanently miss anything
     * written while unwatched. Re-running it here makes the state self-heal on every visit regardless of
     * whether the watcher ever actually attached.
     */
    private suspend fun activate(
        instanceId: InstanceId,
        state: MutableProgressiveStateFlow<Nothing, Map<String, InstalledMod>>
    ) {
        reconcile(instanceId, state)

        val watcherAlreadyStarted = statesLock.withLock { !watchStartedFor.add(instanceId) }
        if (watcherAlreadyStarted) return

        backgroundScope.launch {
            fileWatcherService.watchDirectory(appPaths.modsDir(instanceId)).collect { event ->
                handleModJarEvent(instanceId, state, event)
            }
        }
    }

    /** Full (re)scan: resolves every mod file currently on disk into the in-memory state. */
    private suspend fun reconcile(
        instanceId: InstanceId,
        state: MutableProgressiveStateFlow<Nothing, Map<String, InstalledMod>>
    ) {
        val modsDir = appPaths.modsDir(instanceId)
        if (!modsDir.exists()) {
            modsDir.createDirectories()
            state.update { emptyMap<String, InstalledMod>().successfulWithProgress() }
            return
        }
        val resolved = modsDir.list()
            .filter { isModFile(it.name) }
            .mapNotNull { resolveEntry(it) }
            .associateBy { it.fileName }
        state.update { resolved.successfulWithProgress() }
    }

    private suspend fun handleModJarEvent(
        instanceId: InstanceId,
        state: MutableProgressiveStateFlow<Nothing, Map<String, InstalledMod>>,
        event: FileEvent
    ) {
        if (!isModFile(event.path.name)) return
        val logicalName = modLogicalFileName(event.path.name)
        if (event.type == FileEvent.Type.DELETE) {
            // A DELETE also fires for the "before" half of an enable/disable rename — only really remove the
            // entry if neither the enabled nor the disabled variant exists on disk anymore.
            val stillPresent = existingModFile(instanceId, logicalName)
            if (stillPresent != null) updateEntry(state, stillPresent)
            else state.update { it.map { map -> map - logicalName } }
        } else {
            updateEntry(state, event.path)
        }
    }

    private suspend fun existingModFile(instanceId: InstanceId, logicalName: String): Path? {
        val modsDir = appPaths.modsDir(instanceId)
        val enabledPath = Path(modsDir, logicalName)
        if (enabledPath.exists()) return enabledPath
        val disabledPath = Path(modsDir, "$logicalName.disabled")
        if (disabledPath.exists()) return disabledPath
        return null
    }

    private suspend fun updateEntry(
        state: MutableProgressiveStateFlow<Nothing, Map<String, InstalledMod>>,
        jarPath: Path
    ) {
        val entry = resolveEntry(jarPath) ?: return
        state.update { it.map { map -> map + (entry.fileName to entry) } }
    }

    /** Hashes [jarPath] (memoized within this app run) and resolves it into a fully-built [InstalledMod]. */
    private suspend fun resolveEntry(jarPath: Path): InstalledMod? {
        val size = jarPath.metadataOrNull()?.size ?: return null
        val sha1 = hashOf(jarPath, size)
        val global = globalModMetadataCache.get(sha1) ?: parseAndCacheGlobal(jarPath, sha1)
        return InstalledMod(
            fileName = modLogicalFileName(jarPath.name),
            title = global.title,
            description = global.description,
            version = global.version,
            loader = global.loader?.let { runCatching { ModLoaderType.valueOf(it) }.getOrNull() },
            fileSize = size,
            iconFilePath = global.iconExtension?.let { Path(appPaths.modIconCache, "$sha1.$it").toString() },
            providers = global.providers.map { ModProvider(it.platformId, it.projectId, it.fileId) },
            enabled = isModEnabled(jarPath.name)
        )
    }

    /** Memoized by the *logical* path, so toggling the `.disabled` suffix never busts the memo. */
    private suspend fun hashOf(jarPath: Path, size: Long): String {
        val key = canonicalPath(jarPath)
        hashMemoLock.withLock { hashMemo[key] }?.takeIf { it.size == size }?.let { return it.sha1 }
        val sha1 = jarPath.source { cryptographyService.sha1(it) }.toHexString()
        hashMemoLock.withLock { hashMemo[key] = HashMemo(size, sha1) }
        return sha1
    }

    private fun canonicalPath(jarPath: Path): Path = Path(jarPath.parent!!, modLogicalFileName(jarPath.name))

    private suspend fun parseAndCacheGlobal(jarPath: Path, sha1: String): GlobalModMetadata {
        val parsed = parseModJar(jarPath, json)
        val iconExtension = parsed.iconEntryPath
            ?.let { extractIcon(jarPath, it, appPaths.modIconCache, sha1) }
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotEmpty() }
        val metadata = GlobalModMetadata(
            title = parsed.title,
            description = parsed.description,
            version = parsed.version,
            loader = parsed.loader?.name,
            iconExtension = iconExtension
        )
        globalModMetadataCache.put(sha1, metadata)
        return metadata
    }

    /**
     * Called by [app.vercors.feature.instance.data.repository.InstanceModRepositoryImpl] right after a mod is
     * downloaded via the install flow, to record which platform/project/file it came from — this is the only
     * source of that information, since it can't be derived from the jar's own bytes. Upserts (by [platformId])
     * into the global entry's provider list rather than overwriting it wholesale, since the exact same jar may
     * already carry a provider recorded from a different instance or a different platform. Also refreshes the
     * in-memory state so the mod shows up immediately in the Mods tab, without waiting for the watcher or the
     * next reconcile.
     */
    suspend fun recordOrigin(
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    ) {
        val jarPath = Path(appPaths.modsDir(instanceId), fileName)
        val size = jarPath.metadataOrNull()?.size ?: return
        val sha1 = hashOf(jarPath, size)

        val existing = globalModMetadataCache.get(sha1) ?: parseAndCacheGlobal(jarPath, sha1)
        val provider = ModProviderMetadata(platformId, projectId, fileId)
        val updatedProviders = existing.providers.filterNot { it.platformId == platformId } + provider
        globalModMetadataCache.put(sha1, existing.copy(providers = updatedProviders))

        updateEntry(stateFor(instanceId), jarPath)
    }

    /**
     * Enables/disables a mod by renaming its jar (appending/removing the conventional `.disabled` suffix) and
     * immediately refreshing its entry — not relying on the watcher to eventually catch up.
     */
    suspend fun setEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean) {
        val modsDir = appPaths.modsDir(instanceId)
        val enabledPath = Path(modsDir, fileName)
        val disabledPath = Path(modsDir, "$fileName.disabled")
        val from = if (enabled) disabledPath else enabledPath
        val to = if (enabled) enabledPath else disabledPath
        if (!from.exists()) return
        from.moveTo(to)

        updateEntry(stateFor(instanceId), to)
    }
}
