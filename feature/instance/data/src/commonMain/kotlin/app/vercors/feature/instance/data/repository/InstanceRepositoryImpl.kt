package app.vercors.feature.instance.data.repository

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.core.domain.progressiveStateIn
import app.vercors.feature.instance.data.datasource.InstanceDataSource
import app.vercors.feature.instance.data.dto.toData
import app.vercors.feature.instance.data.dto.toDomain
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.domain.repository.InstanceRepository
import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
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
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.value
import opensavvy.state.progressive.map

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class InstanceRepositoryImpl(
    coroutineScope: CoroutineScope,
    private val dataSource: InstanceDataSource,
) : InstanceRepository {
    override val instances: ProgressiveStateFlow<Nothing, Map<InstanceId, Either<InstanceLoadingError, Instance>>> =
        dataSource.flow.map { outcome ->
            outcome.map { map ->
                map.mapValues { (id, either) ->
                    either.mapLeft { InstanceLoadingError(id, it) }.map { it.toDomain(id) }
                }
            }
        }.progressiveStateIn(coroutineScope)

    private val _statuses = MutableStateFlow<Map<InstanceId, InstanceStatus>>(emptyMap())
    override val statuses = _statuses.asStateFlow()

    private val _jobs = MutableStateFlow<Map<InstanceId, Job>>(emptyMap())

    private val _logs = MutableStateFlow<Map<InstanceId, List<InstanceLogEvent>>>(emptyMap())
    override val logs = _logs.asStateFlow()

    context(_: Raise<InstanceValidationError>)
    override suspend fun saveInstance(instance: Instance) {
        ensure(instance.id.isNotEmpty()) { InstanceValidationError.EmptyInstanceId }
        ensure(instance.name.length >= 3) { InstanceValidationError.NameTooShort }
        val instanceData = instance.toData()
        dataSource.write(instance.id, instanceData)
        logger.info { "Saved instance ${instance.id}" }
    }

    override suspend fun deleteInstance(instance: Instance) {
        dataSource.delete(instance.id)
        logger.info { "Deleted instance ${instance.id}" }
    }

    context(_: Raise<InstanceError>)
    override suspend fun findInstance(id: InstanceId): Instance =
        ensureNotNull(instances.now().value[id]?.bind()) { InstanceNotFound(id) }

    override fun updateInstanceStatus(id: InstanceId, status: InstanceStatus) {
        _statuses.update { it + (id to status) }
    }

    override fun getInstanceStatus(id: InstanceId): InstanceStatus {
        return _statuses.value.getOrElse(id) { InstanceStatus.Idle }
    }

    override fun updateInstanceJob(id: InstanceId, job: Job?) {
        if (job != null) _jobs.update { it + (id to job) }
        else _jobs.update { it - id }
        logger.debug { "Updated instance job $id" }
    }

    override fun cancelInstanceJob(id: InstanceId) {
        _jobs.value[id]?.cancel()
    }

    override suspend fun stopInstance(id: InstanceId) {
        _statuses.value[id]?.let { status ->
            if (status is InstanceStatus.Running) status.stop()
        }
    }

    override suspend fun killInstance(id: InstanceId) {
        _statuses.value[id]?.let { status ->
            if (status is InstanceStatus.Stopping) status.kill()
        }
    }

    override fun addInstanceLog(id: InstanceId, logEvent: InstanceLogEvent) {
        _logs.update { it + (id to it.getOrElse(id, ::emptyList) + logEvent) }
    }

    override fun clearInstanceLogs(id: InstanceId) {
        _logs.update { it - id }
    }
}
