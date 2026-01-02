package app.vercors.feature.game.data.datasource

import app.vercors.core.data.readJson
import app.vercors.core.data.writeFromSource
import app.vercors.core.domain.MutableResourceStateFlow
import app.vercors.core.domain.ResourceStateFlow
import app.vercors.core.domain.asResourceStateFlow
import app.vercors.core.domain.load
import app.vercors.meta.lib.game.MojangVersionManifest
import app.vercors.shared.data.IODispatcher
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.io.Source
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
internal class GameLocalDataSource(
    private val json: Json,
    private val watcher: KfsDirectoryWatcher,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    appPath: Path,
    coroutineScope: CoroutineScope
) {
    private val versionsPath = Path(appPath, "data", "versions")
    private val manifestPath = Path(versionsPath, "manifest.json")

    private val _manifestData = MutableResourceStateFlow<MojangVersionManifest>()
    val manifestData: ResourceStateFlow<MojangVersionManifest> = _manifestData.asResourceStateFlow(coroutineScope)
    private val _manifestWatcher = watcher.onEventFlow.filter { it.path == manifestPath.name }
    private val _manifestMutex = Mutex()

    init {
        coroutineScope.launch {
            watcher.add(versionsPath.toString())
            _manifestWatcher.collectLatest { loadVersionManifest() }
        }
    }

    suspend fun loadVersionManifest() = _manifestData.load(_manifestMutex) { manifestPath.readJson(json, ioDispatcher) }

    suspend fun writeVersionManifest(source: Source) = manifestPath.writeFromSource(source, ioDispatcher)
}
