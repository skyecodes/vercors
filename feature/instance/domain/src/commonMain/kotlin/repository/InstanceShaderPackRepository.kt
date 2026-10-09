package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import opensavvy.state.coroutines.ProgressiveFlow

interface InstanceShaderPackRepository {
    fun observeInstalledShaderPacks(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledContentFile>>

    suspend fun recordShaderPackOrigin(
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    )

    suspend fun setShaderPackEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean)

    suspend fun removeShaderPack(instanceId: InstanceId, fileName: String)

    suspend fun importShaderPack(instanceId: InstanceId, sourcePath: String): Boolean

    suspend fun refreshInstalledShaderPacks(instanceId: InstanceId)
}
