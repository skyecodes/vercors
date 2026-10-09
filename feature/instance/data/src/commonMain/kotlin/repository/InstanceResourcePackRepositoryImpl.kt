package app.vercors.feature.instance.data.repository

import app.vercors.feature.instance.data.content.InstanceResourcePackScannerService
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceResourcePackRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import opensavvy.state.coroutines.ProgressiveFlow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InstanceResourcePackRepositoryImpl(
    private val scannerService: InstanceResourcePackScannerService
) : InstanceResourcePackRepository {
    override fun observeInstalledResourcePacks(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledContentFile>> =
        scannerService.observe(instanceId)

    override suspend fun recordResourcePackOrigin(
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    ): Unit = scannerService.recordOrigin(instanceId, fileName, platformId, projectId, fileId)

    override suspend fun setResourcePackEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean): Unit =
        scannerService.setEnabled(instanceId, fileName, enabled)

    override suspend fun removeResourcePack(instanceId: InstanceId, fileName: String): Unit =
        scannerService.removeFile(instanceId, fileName)

    override suspend fun importResourcePack(instanceId: InstanceId, sourcePath: String): Boolean =
        scannerService.importFile(instanceId, sourcePath)

    override suspend fun refreshInstalledResourcePacks(instanceId: InstanceId): Unit =
        scannerService.refresh(instanceId)
}
