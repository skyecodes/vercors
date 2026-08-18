package app.vercors.feature.instance.data.mods

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.filesystem.FileWatcherService
import app.vercors.core.filesystem.HashedJsonCacheFactory
import app.vercors.feature.instance.data.content.*
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.map

private const val MOD_KIND = "mod"

/**
 * Thin, mod-specific wrapper around [ContentDirScannerEngine] — see that class for how scanning/caching/watching
 * actually works. This class only supplies the mod-specific pieces: the `mods/` directory, the `.jar` extension,
 * the jar-manifest parser ([parseModJar]), and the `mod`-kind cache directories.
 */
@SingleIn(AppScope::class)
@Inject
class InstanceModScannerService(
    appPaths: AppPaths,
    fileWatcherService: FileWatcherService,
    hashedJsonCacheFactory: HashedJsonCacheFactory,
    cryptographyService: CryptographyService,
    backgroundScope: CoroutineScope,
    json: Json
) {
    private val engine = ContentDirScannerEngine(
        instanceDir = { appPaths.modsDir(it) },
        extension = "jar",
        metadataCache = hashedJsonCacheFactory(appPaths.metadataCache(MOD_KIND), ContentFileMetadata.serializer()),
        iconCacheDir = appPaths.iconCache(MOD_KIND),
        parser = ContentFileParser { parseModJar(it, json).toContentInfo() },
        fileWatcherService = fileWatcherService,
        cryptographyService = cryptographyService,
        backgroundScope = backgroundScope
    )

    fun observe(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledContentFile>> =
        engine.observe(instanceId)
            .map { outcome -> outcome.map { entries -> entries.map { it.toInstalledContentFile() } } }

    suspend fun recordOrigin(
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    ) =
        engine.recordOrigin(instanceId, fileName, platformId, projectId, fileId)

    suspend fun setEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean) =
        engine.setEnabled(instanceId, fileName, enabled)
}

private fun ParsedModInfo.toContentInfo() = ParsedContentInfo(
    title = title,
    description = description,
    version = version,
    loader = loader,
    iconEntryPath = iconEntryPath
)
