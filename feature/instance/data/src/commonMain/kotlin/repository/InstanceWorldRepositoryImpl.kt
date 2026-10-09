package app.vercors.feature.instance.data.repository

import app.vercors.feature.instance.data.worlds.InstanceWorldScanner
import app.vercors.feature.instance.domain.model.ImportError
import app.vercors.feature.instance.domain.model.InstalledWorld
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceWorldRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import opensavvy.state.coroutines.ProgressiveFlow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InstanceWorldRepositoryImpl(
    private val scanner: InstanceWorldScanner
) : InstanceWorldRepository {
    override fun observeInstalledWorlds(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledWorld>> =
        scanner.observe(instanceId)

    override suspend fun removeWorld(instanceId: InstanceId, folderName: String): Unit =
        scanner.removeWorld(instanceId, folderName)

    context(_: Raise<ImportError>)
    override suspend fun importWorld(instanceId: InstanceId, sourcePath: String): Unit =
        scanner.importWorld(instanceId, sourcePath)

    override suspend fun refreshInstalledWorlds(instanceId: InstanceId): Unit = scanner.refresh(instanceId)
}
