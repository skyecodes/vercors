package app.vercors.feature.instance.data.servers

import app.vercors.core.domain.MutableProgressiveStateFlow
import app.vercors.core.filesystem.FileWatcherService
import app.vercors.core.filesystem.createDirectories
import app.vercors.core.filesystem.exists
import app.vercors.feature.instance.domain.model.InstalledServer
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.successfulWithProgress

@SingleIn(AppScope::class)
@Inject
class InstanceServerScannerService(
    private val appPaths: AppPaths,
    private val fileWatcherService: FileWatcherService,
    private val backgroundScope: CoroutineScope
) {
    private val statesLock = Mutex()
    private val states = mutableMapOf<InstanceId, MutableProgressiveStateFlow<Nothing, List<InstalledServer>>>()
    private val watchStartedFor = mutableSetOf<InstanceId>()

    fun observe(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledServer>> {
        val state = stateFor(instanceId)
        return state.onStart { backgroundScope.launch { activate(instanceId, state) } }
    }

    private fun stateFor(instanceId: InstanceId): MutableProgressiveStateFlow<Nothing, List<InstalledServer>> =
        states.getOrPut(instanceId) { MutableProgressiveStateFlow() }

    private suspend fun activate(
        instanceId: InstanceId,
        state: MutableProgressiveStateFlow<Nothing, List<InstalledServer>>
    ) {
        reconcile(instanceId, state)

        val watcherAlreadyStarted = statesLock.withLock { !watchStartedFor.add(instanceId) }
        if (watcherAlreadyStarted) return

        backgroundScope.launch {
            fileWatcherService.watchDirectory(appPaths.gameDir(instanceId)).collect {
                reconcile(instanceId, state)
            }
        }
    }

    private suspend fun reconcile(
        instanceId: InstanceId,
        state: MutableProgressiveStateFlow<Nothing, List<InstalledServer>>
    ) {
        // Created before watching: kfswatch never retries a watch on a missing directory
        val gameDir = appPaths.gameDir(instanceId)
        if (!gameDir.exists()) gameDir.createDirectories()
        val file = appPaths.serversDatFile(instanceId)
        val servers = if (file.exists()) parseServersDat(file) else emptyList()
        state.update { servers.successfulWithProgress() }
    }
}
