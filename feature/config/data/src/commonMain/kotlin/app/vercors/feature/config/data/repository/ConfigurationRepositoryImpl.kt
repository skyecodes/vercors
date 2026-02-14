package app.vercors.feature.config.data.repository

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.core.domain.progressiveStateIn
import app.vercors.core.filesystem.FileStoreError
import app.vercors.feature.config.data.datasource.ConfigurationDataSource
import app.vercors.feature.config.data.dto.ConfigurationData
import app.vercors.feature.config.data.dto.toData
import app.vercors.feature.config.data.dto.toDomain
import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.model.ConfigurationError
import app.vercors.feature.config.domain.repository.ConfigurationRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.mapFailure
import opensavvy.state.progressive.onSuccess

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ConfigurationRepositoryImpl(
    coroutineScope: CoroutineScope,
    private val dataSource: ConfigurationDataSource,
) : ConfigurationRepository {
    override val state: ProgressiveStateFlow<ConfigurationError, Configuration> = dataSource.flow
        .map { it.toDomain() }
        .onEach { logger.trace { "Configuration state updated: $it" } }
        .progressiveStateIn(coroutineScope)

    private val mutex = Mutex()

    override suspend fun save(configuration: Configuration) {
        dataSource.write(configuration.toData())
    }

    override suspend fun update(block: Configuration.() -> Configuration) {
        mutex.withLock {
            state.value
                .map { it.block() }
                .onSuccess { save(it) }
        }
    }
}

private fun ProgressiveOutcome<FileStoreError, ConfigurationData>.toDomain() =
    mapFailure { ConfigurationError(it) }.map { it.toDomain() }
