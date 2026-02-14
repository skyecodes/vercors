package app.vercors.feature.config.domain.usecase

import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.repository.ConfigurationRepository
import dev.zacsweers.metro.Inject

@Inject
class UpdateConfigurationUseCase(
    private val configurationRepository: ConfigurationRepository
) {
    suspend operator fun invoke(block: Configuration.() -> Configuration): Unit = configurationRepository.update(block)
}
