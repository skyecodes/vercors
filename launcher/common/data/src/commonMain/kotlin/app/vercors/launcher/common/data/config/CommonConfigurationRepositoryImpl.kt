package app.vercors.launcher.common.data.config

import app.vercors.launcher.common.domain.config.CommonConfiguration
import app.vercors.launcher.common.domain.config.CommonConfigurationRepository
import app.vercors.shared.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

@Single
internal class CommonConfigurationRepositoryImpl(
    coroutineScope: CoroutineScope,
    private val dataSource: CommonConfigurationDataSource
) : CommonConfigurationRepository {
    private val _state: MutableStateFlow<Resource<CommonConfiguration>> = MutableResourceStateFlow()
    override val state: ResourceStateFlow<CommonConfiguration> = _state.asResourceStateFlow(
        coroutineScope = coroutineScope,
        onStart = {
            coroutineScope.launch { load() }
            coroutineScope.launch {
                dataSource.updates.filterNotNull().collect { data ->
                    _state.update { Resource.Success(data.toDomain()) }
                }
            }
        },
        onEach = { logger.trace { "Common configuration: $it" } }
    )
    private val mutex = Mutex()

    override suspend fun load() {
        _state.load(mutex) {
            dataSource.loadFromFile()?.toDomain() ?: CommonConfiguration(testConfig = "Test")
        }
    }

    override suspend fun save(commonConfiguration: CommonConfiguration) {
        dataSource.saveToFile(commonConfiguration.toData())
    }

    override suspend fun update(block: (CommonConfiguration) -> CommonConfiguration) {
        _state.update(mutex) {
            dataSource.saveToFile(block(it).toData())
        }
    }
}