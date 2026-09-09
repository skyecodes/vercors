package app.vercors.feature.instance.data.repository

import app.vercors.feature.instance.data.worlds.InstanceWorldScannerService
import app.vercors.feature.instance.domain.model.InstalledWorld
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceWorldRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import opensavvy.state.coroutines.ProgressiveFlow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class InstanceWorldRepositoryImpl(
    private val scannerService: InstanceWorldScannerService
) : InstanceWorldRepository {
    override fun observeInstalledWorlds(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledWorld>> =
        scannerService.observe(instanceId)

    override suspend fun removeWorld(instanceId: InstanceId, folderName: String) =
        scannerService.removeWorld(instanceId, folderName)

    override suspend fun importWorld(instanceId: InstanceId, sourcePath: String): Boolean =
        scannerService.importWorld(instanceId, sourcePath)

    override suspend fun refreshInstalledWorlds(instanceId: InstanceId) = scannerService.refresh(instanceId)
}
