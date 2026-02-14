package app.vercors.feature.instance.domain.repository

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.feature.instance.domain.model.*
import arrow.core.Either
import arrow.core.raise.Raise
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

interface InstanceRepository {
    val instances: ProgressiveStateFlow<Nothing, Map<InstanceId, Either<InstanceLoadingError, Instance>>>
    val statuses: StateFlow<Map<InstanceId, InstanceStatus>>
    val logs: StateFlow<Map<InstanceId, List<InstanceLogEvent>>>

    context(_: Raise<InstanceValidationError>)
    suspend fun saveInstance(instance: Instance)

    suspend fun deleteInstance(instance: Instance)

    context(_: Raise<InstanceError>)
    suspend fun findInstance(id: InstanceId): Instance

    fun updateInstanceStatus(id: InstanceId, status: InstanceStatus)

    fun getInstanceStatus(id: InstanceId): InstanceStatus

    fun updateInstanceJob(id: InstanceId, job: Job)

    fun cancelInstanceJob(id: InstanceId)

    fun addInstanceLog(id: InstanceId, logEvent: InstanceLogEvent)

    fun clearInstanceLogs(id: InstanceId)
}
