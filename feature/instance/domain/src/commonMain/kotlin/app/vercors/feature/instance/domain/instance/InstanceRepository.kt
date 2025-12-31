package app.vercors.feature.instance.domain.instance

import app.vercors.core.domain.ResourceStateFlow
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId

interface InstanceRepository {
    val instances: ResourceStateFlow<List<Instance>>

    suspend fun reloadInstances()
    suspend fun saveInstance(instance: Instance)
    suspend fun deleteInstance(instance: Instance)
    suspend fun findInstance(id: InstanceId): Instance
}

class InstanceNotFoundException(instanceId: InstanceId) :
    IllegalArgumentException("Instance with ID '$instanceId' not found")
