package app.vercors.feature.instance.data.repository

import app.vercors.core.domain.*
import app.vercors.feature.instance.data.datasource.InstanceDataSource
import app.vercors.feature.instance.data.dto.toData
import app.vercors.feature.instance.data.dto.toDomain
import app.vercors.feature.instance.domain.instance.InstanceNotFoundException
import app.vercors.feature.instance.domain.instance.InstanceRepository
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map

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

    override suspend fun saveInstance(instance: Instance) {
        require(instance.id.isNotEmpty()) { "Instance ID cannot be empty" }
        val instanceData = instance.toData()
        dataSource.saveToFile(instanceData, instance.id)
    }

    override suspend fun deleteInstance(instance: Instance) {
        dataSource.delete(instance.id)
    }

    override suspend fun findInstance(id: InstanceId): Instance =
        instances.await().orThrow().find { it.id == id } ?: throw InstanceNotFoundException(id)
}
