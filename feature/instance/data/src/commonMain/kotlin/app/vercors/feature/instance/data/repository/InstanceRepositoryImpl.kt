package app.vercors.feature.instance.data.repository

import app.vercors.core.domain.*
import app.vercors.feature.instance.data.datasource.InstanceDataSource
import app.vercors.feature.instance.data.dto.toData
import app.vercors.feature.instance.data.dto.toDomain
import app.vercors.feature.instance.domain.instance.InstanceNotFoundException
import app.vercors.feature.instance.domain.instance.InstanceRepository
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

@Single
internal class InstanceRepositoryImpl(
    coroutineScope: CoroutineScope,
    private val dataSource: InstanceDataSource,
) : InstanceRepository {
    private val _instances: MutableResourceStateFlow<List<Instance>> = MutableResourceStateFlow()
    override val instances: ResourceStateFlow<List<Instance>> = _instances
        .asResourceStateFlow(
            coroutineScope = coroutineScope,
            onStart = {
                coroutineScope.launch {
                    dataSource.instancesState.collect { data ->
                        _instances.update {
                            data.map {
                                it.mapNotNull { (key, value) ->
                                    value.orNull()?.toDomain(key)
                                }
                            }
                        }
                    }
                }
            },
            onEach = { logger.debug { "Instances state updated: $it" } },
        )

    override suspend fun saveInstance(instance: Instance) {
        require(instance.id.isNotEmpty()) { "Instance ID cannot be empty" }
        val instanceData = instance.toData()
        dataSource.saveToFile(instanceData, instance.id)
    }

    override suspend fun deleteInstance(instance: Instance) {
        dataSource.delete(instance.id)
    }

    override suspend fun findInstance(id: InstanceId): Instance =
        _instances.await().orThrow().find { it.id == id } ?: throw InstanceNotFoundException(id)
}
