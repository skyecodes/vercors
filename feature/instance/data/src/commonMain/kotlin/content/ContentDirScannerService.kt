package app.vercors.feature.instance.data.content

import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import kotlinx.coroutines.flow.map
import kotlinx.io.files.Path
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.map

/**
 * Shared delegate behind the per-content-kind scanner services (mods, resource packs, shader packs) — each of
 * those only supplies its own [ContentDirScannerEngine] (directory, extension, parser, cache-kind), then exposes
 * the same five operations via this base class.
 */
abstract class ContentDirScannerService internal constructor(private val engine: ContentDirScannerEngine) {
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

    suspend fun removeFile(instanceId: InstanceId, fileName: String) =
        engine.removeFile(instanceId, fileName)

    suspend fun importFile(instanceId: InstanceId, sourcePath: String): Boolean =
        engine.importFile(instanceId, Path(sourcePath))
}
