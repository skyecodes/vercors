package app.vercors.feature.project.domain.repository

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.model.pack.InstalledPack

interface InstalledPackRepository {
    suspend fun get(instanceId: InstanceId): InstalledPack?

    suspend fun save(instanceId: InstanceId, pack: InstalledPack)

    suspend fun deleteGameFile(instanceId: InstanceId, path: String)
}
