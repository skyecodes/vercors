package app.vercors.feature.instance.data.repository

import app.vercors.core.domain.*
import app.vercors.feature.instance.data.datasource.InstanceRepositoryDataSource
import app.vercors.feature.instance.data.dto.toData
import app.vercors.feature.instance.data.dto.toDomain
import app.vercors.feature.instance.domain.instance.InstanceNotFoundException
import app.vercors.feature.instance.domain.instance.InstanceRepository
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

@Single
internal class InstanceRepositoryImpl(
    backgroundScope: CoroutineScope,
    private val dataSource: InstanceRepositoryDataSource,
) : InstanceRepository {
    private val _instances: MutableResourceStateFlow<List<Instance>> = MutableResourceStateFlow()
    override val instances: ResourceStateFlow<List<Instance>> = _instances
        .asResourceStateFlow(
            coroutineScope = backgroundScope,
            onStart = { backgroundScope.launch { reloadInstances() } },
            onEach = { logger.debug { "Instances state updated: $it" } },
        )
    private val instancesMutex = Mutex()

    override suspend fun reloadInstances() {
        _instances.load(instancesMutex) {
            logger.info { "Reloading instances" }
            dataSource.loadAll()
                .map { (id, instance) -> instance.toDomain(id) }
                .also { logger.info { "Instances reloaded" } }
        }
    }

    override suspend fun saveInstance(instance: Instance) {
        require(instance.id.isNotEmpty()) { "Instance ID cannot be empty" }
        val instanceData = instance.toData()
        dataSource.saveToFile(instanceData, instance.id)
        _instances.addToOrUpdateList(instance) { a, b -> a.id == b.id }
    }

    override suspend fun deleteInstance(instance: Instance) {
        dataSource.delete(instance.id)
        _instances.removeFromList(instance) { a, b -> a.id == b.id }
    }

    override suspend fun findInstance(id: InstanceId): Instance =
        _instances.await().orThrow().find { it.id == id } ?: throw InstanceNotFoundException(id)
}
