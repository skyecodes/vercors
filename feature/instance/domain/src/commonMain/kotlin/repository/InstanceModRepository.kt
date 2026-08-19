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

    /** [fileName] is the mod's stable, "logical" name — never includes the `.disabled` suffix used on disk. */
    suspend fun setModEnabled(instanceId: InstanceId, fileName: String, enabled: Boolean)

    /** Permanently deletes the mod file from disk. [fileName] is the logical name, as with [setModEnabled]. */
    suspend fun removeMod(instanceId: InstanceId, fileName: String)
}
