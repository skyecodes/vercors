package app.vercors.feature.instance.data.repository

import app.vercors.feature.instance.data.mods.InstanceModScannerService
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceModRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import opensavvy.state.coroutines.ProgressiveFlow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class InstanceModRepositoryImpl(
    private val scannerService: InstanceModScannerService
) : InstanceModRepository {
    override fun observeInstalledMods(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledContentFile>> =
        scannerService.observe(instanceId)

    override suspend fun recordModOrigin(
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    ) = scannerService.recordOrigin(instanceId, fileName, platformId, projectId, fileId)

    override suspend fun setModEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean) =
        scannerService.setEnabled(instanceId, fileName, enabled)

    override suspend fun removeMod(instanceId: InstanceId, fileName: String) =
        scannerService.removeFile(instanceId, fileName)
}
