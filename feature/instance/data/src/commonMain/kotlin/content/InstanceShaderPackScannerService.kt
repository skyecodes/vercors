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
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.map

private const val SHADER_PACK_KIND = "shaderpack"

/**
 * Thin, shaderpack-specific wrapper around [ContentDirScannerEngine] — see that class for how scanning/caching/
 * watching actually works. This class only supplies the shaderpack-specific pieces: the `shaderpacks/` directory,
 * the `.zip` extension, [ShaderPackParser], and the `shaderpack`-kind cache directories.
 */
@SingleIn(AppScope::class)
@Inject
class InstanceShaderPackScannerService(
    appPaths: AppPaths,
    fileWatcherService: FileWatcherService,
    hashedJsonCacheFactory: HashedJsonCacheFactory,
    cryptographyService: CryptographyService,
    backgroundScope: CoroutineScope
) {
    private val engine = ContentDirScannerEngine(
        instanceDir = { appPaths.shaderPacksDir(it) },
        extension = "zip",
        metadataCache = hashedJsonCacheFactory(
            appPaths.metadataCache(SHADER_PACK_KIND),
            ContentFileMetadata.serializer()
        ),
        iconCacheDir = appPaths.iconCache(SHADER_PACK_KIND),
        parser = ShaderPackParser,
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
