package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import opensavvy.state.coroutines.ProgressiveFlow

interface InstanceModRepository {
    fun observeInstalledMods(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledContentFile>>

    suspend fun recordModOrigin(
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    )

    suspend fun setModEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean)

    suspend fun removeMod(instanceId: InstanceId, fileName: String)

    suspend fun importMod(instanceId: InstanceId, sourcePath: String): Boolean

    suspend fun refreshInstalledMods(instanceId: InstanceId)
}
