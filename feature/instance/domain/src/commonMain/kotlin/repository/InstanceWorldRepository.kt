package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.ImportError
import app.vercors.feature.instance.domain.model.InstalledWorld
import app.vercors.feature.instance.domain.model.InstanceId
import arrow.core.raise.Raise
import opensavvy.state.coroutines.ProgressiveFlow

interface InstanceWorldRepository {
    fun observeInstalledWorlds(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledWorld>>

    suspend fun removeWorld(instanceId: InstanceId, folderName: String)

    context(_: Raise<ImportError>)
    suspend fun importWorld(instanceId: InstanceId, sourcePath: String)

    suspend fun refreshInstalledWorlds(instanceId: InstanceId)
}
