package app.vercors.feature.config.data.repository

import app.vercors.core.domain.*
import app.vercors.feature.config.data.datasource.AbstractConfigurationDataSource
import app.vercors.feature.config.domain.repository.AbstractConfigurationRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

private val logger = KotlinLogging.logger {}

abstract class AbstractConfigurationRepositoryImpl<T : Any, U : Any>(
    coroutineScope: CoroutineScope,
    private val dataSource: AbstractConfigurationDataSource<U>,
    private val toDomain: U.() -> T,
    private val toData: T.() -> U,
) : AbstractConfigurationRepository<T> {
    private val _state: MutableResourceStateFlow<T> = MutableResourceStateFlow()
    override val state: ResourceStateFlow<T> = _state.asResourceStateFlow(
        coroutineScope = coroutineScope,
        onStart = {
            coroutineScope.launch {
                dataSource.configState.collect { data ->
                    _state.update { data.map { it.toDomain() } }
                }
            }
        },
        onEach = { logger.debug { "Configuration state updated: $it" } }
    )
    private val mutex = Mutex()

    override suspend fun save(commonConfiguration: T) {
        dataSource.saveToFile(commonConfiguration.toData())
    }

    override suspend fun update(block: (T) -> T) {
        _state.read(mutex) {
            dataSource.saveToFile(block(it).toData())
        }
    }
}
