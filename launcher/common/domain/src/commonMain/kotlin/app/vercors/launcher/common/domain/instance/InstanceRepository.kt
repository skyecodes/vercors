package app.vercors.launcher.common.domain.instance

import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.model.InstanceId
import app.vercors.shared.domain.ResourceStateFlow

interface InstanceRepository {
    val instances: ResourceStateFlow<List<Instance>>

    suspend fun reloadInstances()
    suspend fun saveInstance(instance: Instance)
    suspend fun deleteInstance(instance: Instance)
    suspend fun findInstance(id: InstanceId): Instance
}
