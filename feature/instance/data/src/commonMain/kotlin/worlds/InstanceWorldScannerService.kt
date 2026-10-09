package app.vercors.feature.instance.data.worlds

import app.vercors.core.domain.MutableProgressiveStateFlow
import app.vercors.core.filesystem.*
import app.vercors.feature.instance.domain.model.ImportError
import app.vercors.feature.instance.domain.model.InstalledWorld
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.extractWorldZip
import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.raise
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

    // For bulk writes outside importWorld: never rely on the watcher alone
    suspend fun refresh(instanceId: InstanceId) = reconcile(instanceId, stateFor(instanceId))

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

    suspend fun removeWorld(instanceId: InstanceId, folderName: String) {
        val worldDir = appPaths.worldDir(instanceId, folderName)
        if (worldDir.exists()) worldDir.deleteRecursively()
        stateFor(instanceId).update { it.map { map -> map - folderName } }
    }

    context(_: Raise<ImportError>)
    suspend fun importWorld(instanceId: InstanceId, sourcePath: String) {
        val zipPath = Path(sourcePath)
        ensure(zipPath.name.endsWith(".zip")) { ImportError.UnsupportedFileType }
        try {
            appPaths.extractWorldZip(zipPath, instanceId, zipPath.name)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            raise(ImportError.CopyFailed)
        }
        reconcile(instanceId, stateFor(instanceId))
    }
}
