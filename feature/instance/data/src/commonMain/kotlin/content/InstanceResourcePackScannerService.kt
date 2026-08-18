package app.vercors.feature.instance.data.content

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.filesystem.FileWatcherService
import app.vercors.core.filesystem.HashedJsonCacheFactory
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

private const val RESOURCE_PACK_KIND = "resourcepack"

/**
 * Thin, resourcepack-specific wrapper around [ContentDirScannerEngine] — see that class for how scanning/caching/
 * watching actually works. This class only supplies the resourcepack-specific pieces: the `resourcepacks/`
 * directory, the `.zip` extension, [ResourcePackParser], and the `resourcepack`-kind cache directories.
 */
@SingleIn(AppScope::class)
@Inject
class InstanceResourcePackScannerService(
    appPaths: AppPaths,
    fileWatcherService: FileWatcherService,
    hashedJsonCacheFactory: HashedJsonCacheFactory,
    cryptographyService: CryptographyService,
    backgroundScope: CoroutineScope,
    json: Json
) {
    private val engine = ContentDirScannerEngine(
        instanceDir = { appPaths.resourcePacksDir(it) },
        extension = "zip",
        metadataCache = hashedJsonCacheFactory(
            appPaths.metadataCache(RESOURCE_PACK_KIND),
            ContentFileMetadata.serializer()
        ),
        iconCacheDir = appPaths.iconCache(RESOURCE_PACK_KIND),
        parser = ResourcePackParser(json),
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
