package app.vercors.launcher.common.data.instance

import app.vercors.launcher.common.domain.instance.Instance
import app.vercors.launcher.common.domain.instance.InstanceRepository
import app.vercors.shared.domain.*
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
            onEach = { logger.debug { "Instances: $it" } },
        )
    private val instancesMutex = Mutex()

    override suspend fun reloadInstances() {
        _instances.load(instancesMutex) {
            logger.debug { "Reloading instances" }
            dataSource.loadAll()
                .map { (id, instance) -> instance.toDomain(id) }
                .also { logger.debug { "Instances reloaded" } }
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
}



