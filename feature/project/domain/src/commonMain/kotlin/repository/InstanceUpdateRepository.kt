package app.vercors.feature.project.domain.repository

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.model.update.InstanceUpdates
import kotlinx.coroutines.flow.Flow

interface InstanceUpdateRepository {
    fun observeAll(): Flow<Map<InstanceId, InstanceUpdates>>

    fun get(instanceId: InstanceId): InstanceUpdates?

    fun set(instanceId: InstanceId, updates: InstanceUpdates)

    fun removeContent(instanceId: InstanceId, fileNames: Set<String>)

    fun clear(instanceId: InstanceId)
}
