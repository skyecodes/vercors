package app.vercors.feature.game.data.datasource

import app.vercors.core.data.exists
import app.vercors.core.data.readJson
import app.vercors.core.data.source
import app.vercors.core.data.writeFromSource
import app.vercors.core.domain.MutableResourceStateFlow
import app.vercors.core.domain.ResourceStateFlow
import app.vercors.core.domain.asResourceStateFlow
import app.vercors.core.domain.load
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.GameVersionList
import app.vercors.meta.lib.game.MojangAssetIndex
import app.vercors.meta.lib.game.MojangVersionInfo
import app.vercors.meta.lib.game.MojangVersionManifest
import dev.whyoleg.cryptography.operations.Hasher
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.SingleIn
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.io.Source
import kotlinx.io.bytestring.toHexString
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@Inject
class GameLocalDataSource(
    private val json: Json,
    @Named("sha1") private val sha1Hasher: Hasher,
    watcher: KfsDirectoryWatcher,
    appPath: Path,
    coroutineScope: CoroutineScope
) {
    private val assetIndexesPath = Path(appPath, "data", "assets", "indexes")
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
        coroutineScope.launch { loadVersionManifest() }
    }

    suspend fun loadVersionManifest() = _manifestData.load(_manifestMutex) { manifestPath.readJson(json) }

    suspend fun writeVersionManifest(source: Source) = manifestPath.writeFromSource(source)

    private fun getVersionPath(versionId: String) = Path(versionsPath, versionId, "${versionId}.json")

    suspend fun checkAndLoadVersionDetails(version: GameVersionList.Version): MojangVersionInfo? {
        val versionPath = getVersionPath(version.id)
        if (versionPath.exists()) {
            val hash = versionPath.source { sha1Hasher.hash(it) }.toHexString()
            if (hash == version.sha1)
                return versionPath.readJson(json)
        }
        return null
    }

    suspend fun writeVersionDetails(source: Source, versionId: String) {
        val versionPath = getVersionPath(versionId)
        versionPath.writeFromSource(source)
    }

    private fun getAssetIndexPath(assetIndexId: String) = Path(assetIndexesPath, "${assetIndexId}.json")

    suspend fun checkAndLoadAssetIndex(assetIndex: GameVersionDetails.AssetIndex): MojangAssetIndex? {
        val assetIndexPath = getAssetIndexPath(assetIndex.id)
        if (assetIndexPath.exists()) {
            val hash = assetIndexPath.source { sha1Hasher.hash(it) }.toHexString()
            if (hash == assetIndex.sha1)
                return assetIndexPath.readJson(json)
        }
        return null
    }

    suspend fun writeAssetIndex(source: Source, assetIndexId: String) {
        val assetIndexPath = getAssetIndexPath(assetIndexId)
        assetIndexPath.writeFromSource(source)
    }
}
