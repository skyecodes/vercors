package app.vercors.feature.instance.data.content

import app.vercors.feature.instance.domain.model.ImportError
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import arrow.core.raise.Raise
import kotlinx.coroutines.flow.map
import kotlinx.io.files.Path
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.map

abstract class ContentDirScannerService internal constructor(private val engine: ContentDirScannerEngine) {
    fun observe(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledContentFile>> =
        engine.observe(instanceId)
            .map { outcome -> outcome.map { entries -> entries.map { it.toDomain() } } }

    suspend fun recordOrigin(
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    ): Unit =
        engine.recordOrigin(instanceId, fileName, platformId, projectId, fileId)

    suspend fun setEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean): Unit =
        engine.setEnabled(instanceId, fileName, enabled)

    suspend fun removeFile(instanceId: InstanceId, fileName: String): Unit =
        engine.removeFile(instanceId, fileName)

    context(_: Raise<ImportError>)
    suspend fun importFile(instanceId: InstanceId, sourcePath: String): Unit =
        engine.importFile(instanceId, Path(sourcePath))

    suspend fun refresh(instanceId: InstanceId): Unit = engine.refresh(instanceId)
}
