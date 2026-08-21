package app.vercors.feature.instance.data.repository

import app.vercors.feature.instance.data.content.InstanceShaderPackScannerService
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceShaderPackRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import opensavvy.state.coroutines.ProgressiveFlow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class InstanceShaderPackRepositoryImpl(
    private val scannerService: InstanceShaderPackScannerService
) : InstanceShaderPackRepository {
    override fun observeInstalledShaderPacks(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledContentFile>> =
        scannerService.observe(instanceId)

    override suspend fun recordShaderPackOrigin(
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    ) = scannerService.recordOrigin(instanceId, fileName, platformId, projectId, fileId)

    override suspend fun setShaderPackEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean) =
        scannerService.setEnabled(instanceId, fileName, enabled)

    override suspend fun removeShaderPack(instanceId: InstanceId, fileName: String) =
        scannerService.removeFile(instanceId, fileName)

    override suspend fun importShaderPack(instanceId: InstanceId, sourcePath: String): Boolean =
        scannerService.importFile(instanceId, sourcePath)
}
