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

    suspend fun deleteInstance(instance: Instance)

    /** Deletes an instance's on-disk game directory — [deleteInstance] only removes its metadata. */
    suspend fun deleteInstanceFiles(instanceId: InstanceId)

    context(_: Raise<InstanceError>)
    suspend fun findInstance(id: InstanceId): Instance

    /** Copies [sourcePath] (an arbitrary local file, never deleted) into the instance's managed icon storage. */
    context(_: Raise<InstanceError>)
    suspend fun setInstanceIcon(instanceId: InstanceId, sourcePath: String)

    /** Best-effort: downloads [url] into the instance's managed icon storage. Never raises — returns `false` on failure. */
    suspend fun setInstanceIconFromUrl(instanceId: InstanceId, url: String): Boolean

    /** Deletes the instance's custom icon (if any) and reverts it to the default icon. */
    context(_: Raise<InstanceError>)
    suspend fun removeInstanceIcon(instanceId: InstanceId)

    fun updateInstanceStatus(id: InstanceId, status: InstanceStatus)

    fun getInstanceStatus(id: InstanceId): InstanceStatus

    fun updateInstanceJob(id: InstanceId, job: Job?)

    fun cancelInstanceJob(id: InstanceId)

    suspend fun stopInstance(id: InstanceId)

    suspend fun killInstance(id: InstanceId)

    fun addInstanceLog(id: InstanceId, logEvent: InstanceLogEvent)

    fun clearInstanceLogs(id: InstanceId)

    fun updateManualDownloadItems(id: InstanceId, items: List<ManualDownloadItem>)
}
