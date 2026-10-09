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
    val manualDownloads: StateFlow<Map<InstanceId, List<ManualDownloadItem>>>

    context(_: Raise<InstanceValidationError>)
    suspend fun saveInstance(instance: Instance)

    suspend fun deleteInstance(instanceId: InstanceId)

    suspend fun deleteInstanceFiles(instanceId: InstanceId)

    suspend fun copyInstanceFiles(sourceId: InstanceId, targetId: InstanceId)

    context(_: Raise<InstanceError>)
    suspend fun findInstance(id: InstanceId): Instance

    context(_: Raise<InstanceError>)
    suspend fun setInstanceIcon(instanceId: InstanceId, sourcePath: String)

    // Takes the full instance: the reactive instances map may lag behind a fresh write
    context(_: Raise<InstanceIconError>)
    suspend fun setInstanceIconFromUrl(instance: Instance, url: String)

    context(_: Raise<InstanceError>)
    suspend fun removeInstanceIcon(instanceId: InstanceId)

    context(_: Raise<InstanceFolderError>)
    suspend fun openInstanceFolder(instanceId: InstanceId, folder: InstanceFolder)

    fun updateInstanceStatus(id: InstanceId, status: InstanceStatus)

    fun getInstanceStatus(id: InstanceId): InstanceStatus

    fun updateInstanceJob(id: InstanceId, job: Job?)

    fun cancelInstanceJob(id: InstanceId)

    fun hasActiveJob(id: InstanceId): Boolean

    suspend fun stopInstance(id: InstanceId)

    suspend fun killInstance(id: InstanceId)

    fun addInstanceLog(id: InstanceId, logEvent: InstanceLogEvent)

    fun clearInstanceLogs(id: InstanceId)

    fun updateManualDownloadItems(id: InstanceId, items: List<ManualDownloadItem>)
}
