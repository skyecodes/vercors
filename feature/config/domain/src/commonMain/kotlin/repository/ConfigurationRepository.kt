package app.vercors.feature.config.domain.repository

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.model.ConfigurationError

interface ConfigurationRepository {
    val state: ProgressiveStateFlow<ConfigurationError, Configuration>

    suspend fun save(configuration: Configuration)
    suspend fun update(block: Configuration.() -> Configuration)
}
