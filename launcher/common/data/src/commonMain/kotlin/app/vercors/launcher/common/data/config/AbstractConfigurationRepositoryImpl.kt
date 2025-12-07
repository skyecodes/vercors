package app.vercors.launcher.common.data.config

import app.vercors.launcher.common.domain.config.AbstractConfigurationRepository
import app.vercors.shared.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

private val logger = KotlinLogging.logger {}

abstract class AbstractConfigurationRepositoryImpl<T : Any, U : Any>(
    coroutineScope: CoroutineScope,
    private val dataSource: AbstractConfigurationDataSource<U>,
    private val toDomain: U.() -> T,
    private val toData: T.() -> U,
    private val default: () -> T
) : AbstractConfigurationRepository<T> {
    private val _state: MutableResourceStateFlow<T> = MutableResourceStateFlow()
    override val state: ResourceStateFlow<T> = _state.asResourceStateFlow(
        coroutineScope = coroutineScope,
        onStart = {
            coroutineScope.launch { load() }
            coroutineScope.launch {
                dataSource.updates.filterNotNull().collect { data ->
                    _state.update { Resource.Success(data.toDomain()) }
                }
            }
        },
        onEach = { logger.debug { "Configuration: $it" } }
    )
    private val mutex = Mutex()

    override suspend fun load() {
        _state.load(mutex) {
            dataSource.loadFromFile()?.toDomain() ?: default()
        }
    }

    override suspend fun save(commonConfiguration: T) {
        dataSource.saveToFile(commonConfiguration.toData())
    }

    override suspend fun update(block: (T) -> T) {
        _state.update(mutex) {
            dataSource.saveToFile(block(it).toData())
        }
    }
}
