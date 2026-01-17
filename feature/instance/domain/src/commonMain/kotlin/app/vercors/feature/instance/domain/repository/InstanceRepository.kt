package app.vercors.feature.instance.domain.repository

import app.vercors.core.domain.ResourceStateFlow
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceLogEvent
import app.vercors.feature.instance.domain.model.InstanceStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

interface InstanceRepository {
    val instances: ResourceStateFlow<List<Instance>>
    val statuses: StateFlow<Map<InstanceId, InstanceStatus>>
    val logs: StateFlow<Map<InstanceId, List<InstanceLogEvent>>>

    suspend fun saveInstance(instance: Instance)
    suspend fun deleteInstance(instance: Instance)
    suspend fun findInstance(id: InstanceId): Instance
    fun updateInstanceStatus(id: InstanceId, status: InstanceStatus)
    fun getInstanceStatus(id: InstanceId): InstanceStatus
    fun updateInstanceJob(id: InstanceId, job: Job)
    fun cancelInstanceJob(id: InstanceId)
    fun addInstanceLog(id: InstanceId, logEvent: InstanceLogEvent)
    fun clearInstanceLogs(id: InstanceId)
}

class InstanceNotFoundException(instanceId: InstanceId) :
    IllegalArgumentException("Instance with ID '$instanceId' not found")
