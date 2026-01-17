package app.vercors.feature.instance.data.repository

import app.vercors.core.domain.*
import app.vercors.feature.instance.data.datasource.InstanceDataSource
import app.vercors.feature.instance.data.dto.toData
import app.vercors.feature.instance.data.dto.toDomain
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceLogEvent
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.repository.InstanceNotFoundException
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class InstanceRepositoryImpl(
    coroutineScope: CoroutineScope,
    private val dataSource: InstanceDataSource,
) : InstanceRepository {
    override val instances: ResourceStateFlow<List<Instance>> = dataSource.instancesState.map { data ->
        data.map {
            it.mapNotNull { (key, value) ->
                value.orNull()?.toDomain(key)
            }
        }
    }.resourceStateIn(coroutineScope)

    private val _statuses = MutableStateFlow<Map<InstanceId, InstanceStatus>>(emptyMap())
    override val statuses = _statuses.asStateFlow()

    private val _jobs = MutableStateFlow<Map<InstanceId, Job>>(emptyMap())

    private val _logs = MutableStateFlow<Map<InstanceId, List<InstanceLogEvent>>>(emptyMap())
    override val logs = _logs.asStateFlow()

    override suspend fun saveInstance(instance: Instance) {
        require(instance.id.isNotEmpty()) { "Instance ID cannot be empty" }
        val instanceData = instance.toData()
        dataSource.saveToFile(instanceData, instance.id)
        logger.info { "Saved instance ${instance.id}" }
    }

    override suspend fun deleteInstance(instance: Instance) {
        dataSource.delete(instance.id)
        logger.info { "Deleted instance ${instance.id}" }
    }

    override suspend fun findInstance(id: InstanceId): Instance =
        instances.await().orThrow().find { it.id == id } ?: throw InstanceNotFoundException(id)

    override fun updateInstanceStatus(id: InstanceId, status: InstanceStatus) {
        _statuses.update { it + (id to status) }
    }

    override fun getInstanceStatus(id: InstanceId): InstanceStatus {
        return _statuses.value.getOrElse(id) { InstanceStatus.Idle }
    }

    override fun updateInstanceJob(id: InstanceId, job: Job) {
        job.invokeOnCompletion { error ->
            logger.debug { "Instance job completed with $error" }
            _jobs.update { it - id }
            if (error == null || error is CancellationException) {
                updateInstanceStatus(id, InstanceStatus.Idle)
            } else if (error is Exception) {
                updateInstanceStatus(id, InstanceStatus.Failed.Unknown(error))
            }
        }
        _jobs.update { it + (id to job) }
        logger.debug { "Updated instance job $id" }
    }

    override fun cancelInstanceJob(id: InstanceId) {
        _jobs.value[id]?.cancel()
    }

    override fun addInstanceLog(id: InstanceId, logEvent: InstanceLogEvent) {
        _logs.update { it + (id to it.getOrElse(id, ::emptyList) + logEvent) }
    }

    override fun clearInstanceLogs(id: InstanceId) {
        _logs.update { it - id }
    }
}
