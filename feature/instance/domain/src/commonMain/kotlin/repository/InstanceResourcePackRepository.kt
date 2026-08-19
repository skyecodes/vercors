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

    /** [fileName] is the pack's stable, "logical" name — never includes the `.disabled` suffix used on disk. */
    suspend fun setResourcePackEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean)

    /** Permanently deletes the pack file from disk. [fileName] is the logical name, as with [setResourcePackEnabled]. */
    suspend fun removeResourcePack(instanceId: InstanceId, fileName: String)
}
