package app.vercors.feature.instance.data.worlds

import app.vercors.core.domain.MutableProgressiveStateFlow
import app.vercors.core.filesystem.*
import app.vercors.feature.instance.domain.model.InstalledWorld
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.extractWorldZip
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.map
import opensavvy.state.progressive.successfulWithProgress

/**
 * Reactive scanner over an instance's `saves/` directory, backing the Worlds tab. Deliberately much smaller than
 * [app.vercors.feature.instance.data.content.ContentDirScannerEngine]: a world is a *directory*, not a single
 * hashable file, so there's no hashing, no shared metadata cache, and no `.disabled`-suffix enable/disable concept
 * to maintain — each entry is resolved straight from the world folder's own `icon.png`/`level.dat`. A watcher
 * event on `saves/` just triggers a full re-list (cheap: only top-level folder names + two `exists`/`mtime`
 * checks per world, no per-byte hashing like the mod/resourcepack/shaderpack scanners need).
 */
@SingleIn(AppScope::class)
@Inject
class InstanceWorldScannerService(
    private val appPaths: AppPaths,
    private val fileWatcherService: FileWatcherService,
    private val backgroundScope: CoroutineScope
) {
    private val statesLock = Mutex()
    private val states = mutableMapOf<InstanceId, MutableProgressiveStateFlow<Nothing, Map<String, InstalledWorld>>>()
    private val watchStartedFor = mutableSetOf<InstanceId>()

    fun observe(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledWorld>> {
        val state = stateFor(instanceId)
        return state.onStart { backgroundScope.launch { activate(instanceId, state) } }
            .map { outcome -> outcome.map { it.values.toList() } }
    }

    private fun stateFor(instanceId: InstanceId): MutableProgressiveStateFlow<Nothing, Map<String, InstalledWorld>> =
        states.getOrPut(instanceId) { MutableProgressiveStateFlow() }

    private suspend fun activate(
        instanceId: InstanceId,
        state: MutableProgressiveStateFlow<Nothing, Map<String, InstalledWorld>>
    ) {
        reconcile(instanceId, state)

        val watcherAlreadyStarted = statesLock.withLock { !watchStartedFor.add(instanceId) }
        if (watcherAlreadyStarted) return

        backgroundScope.launch {
            fileWatcherService.watchDirectory(appPaths.savesDir(instanceId)).collect {
                reconcile(instanceId, state)
            }
        }
    }

    /** Full (re)scan: resolves every world directory currently on disk into the in-memory state. */
    private suspend fun reconcile(
        instanceId: InstanceId,
        state: MutableProgressiveStateFlow<Nothing, Map<String, InstalledWorld>>
    ) {
        val dir = appPaths.savesDir(instanceId)
        if (!dir.exists()) {
            dir.createDirectories()
            state.update { emptyMap<String, InstalledWorld>().successfulWithProgress() }
            return
        }
        val resolved = dir.list()
            .filter { it.isDirectory() && Path(it, "level.dat").exists() }
            .map { toInstalledWorld(it) }
            .associateBy { it.folderName }
        state.update { resolved.successfulWithProgress() }
    }

    private suspend fun toInstalledWorld(worldDir: Path): InstalledWorld {
        val iconPath = Path(worldDir, "icon.png")
        return InstalledWorld(
            folderName = worldDir.name,
            iconFilePath = iconPath.takeIf { it.exists() }?.toString(),
            lastModified = Path(worldDir, "level.dat").lastModifiedOrNull()
        )
    }

    /** Permanently deletes a world's directory (and everything in it) and drops its entry. */
    suspend fun removeWorld(instanceId: InstanceId, folderName: String) {
        val worldDir = appPaths.worldDir(instanceId, folderName)
        if (worldDir.exists()) worldDir.deleteRecursively()
        stateFor(instanceId).update { it.map { map -> map - folderName } }
    }

    /**
     * Extracts [sourcePath] (a local `.zip` of a save folder, never deleted) into [instanceId]'s `saves/`
     * directory as a new world, then immediately reconciles — not relying on the watcher to eventually catch up.
     * Returns `false` without extracting anything if [sourcePath] isn't a `.zip` file, or if extraction fails for
     * any reason (e.g. a corrupt/invalid archive).
     */
    suspend fun importWorld(instanceId: InstanceId, sourcePath: String): Boolean {
        val zipPath = Path(sourcePath)
        if (!zipPath.name.endsWith(".zip")) return false
        try {
            appPaths.extractWorldZip(zipPath, instanceId, zipPath.name)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return false
        }
        reconcile(instanceId, stateFor(instanceId))
        return true
    }
}
