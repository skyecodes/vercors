package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import opensavvy.state.coroutines.ProgressiveFlow

interface InstanceResourcePackRepository {
    fun observeInstalledResourcePacks(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledContentFile>>

    suspend fun recordResourcePackOrigin(
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    )

    suspend fun setResourcePackEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean)

    suspend fun removeResourcePack(instanceId: InstanceId, fileName: String)

    suspend fun importResourcePack(instanceId: InstanceId, sourcePath: String): Boolean

    suspend fun refreshInstalledResourcePacks(instanceId: InstanceId)
}
