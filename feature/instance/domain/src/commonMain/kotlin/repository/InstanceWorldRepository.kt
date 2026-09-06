package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.InstalledWorld
import app.vercors.feature.instance.domain.model.InstanceId
import opensavvy.state.coroutines.ProgressiveFlow

interface InstanceWorldRepository {
    fun observeInstalledWorlds(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledWorld>>

    /** Permanently deletes the world's directory (and everything in it) from disk. */
    suspend fun removeWorld(instanceId: InstanceId, folderName: String)

    /** Extracts [sourcePath] (a local `.zip` of a save folder, never deleted) into [instanceId]'s `saves/`
     *  directory as a new world. Returns `false` without extracting anything if [sourcePath] isn't a `.zip` file. */
    suspend fun importWorld(instanceId: InstanceId, sourcePath: String): Boolean
}
