package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.InstalledWorld
import app.vercors.feature.instance.domain.model.InstanceId
import opensavvy.state.coroutines.ProgressiveFlow

interface InstanceWorldRepository {
    fun observeInstalledWorlds(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledWorld>>

    suspend fun removeWorld(instanceId: InstanceId, folderName: String)

    suspend fun importWorld(instanceId: InstanceId, sourcePath: String): Boolean

    suspend fun refreshInstalledWorlds(instanceId: InstanceId)
}
