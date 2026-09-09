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

    /** [fileName] is the pack's stable, "logical" name — never includes the `.disabled` suffix used on disk. */
    suspend fun setShaderPackEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean)

    /** Permanently deletes the pack file from disk. [fileName] is the logical name, as with [setShaderPackEnabled]. */
    suspend fun removeShaderPack(instanceId: InstanceId, fileName: String)

    /** Copies [sourcePath] (an arbitrary local file, never deleted) into the instance's shader packs directory. */
    suspend fun importShaderPack(instanceId: InstanceId, sourcePath: String): Boolean

    /** Forces an immediate rescan, for a caller that just wrote a batch of files outside the APIs above. */
    suspend fun refreshInstalledShaderPacks(instanceId: InstanceId)
}
