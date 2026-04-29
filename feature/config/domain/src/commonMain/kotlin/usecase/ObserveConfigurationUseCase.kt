package app.vercors.feature.config.domain.usecase

import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.model.ConfigurationError
import app.vercors.feature.config.domain.repository.ConfigurationRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveConfigurationUseCase(
    private val configurationRepository: ConfigurationRepository
) {
    operator fun invoke(): ProgressiveFlow<ConfigurationError, Configuration> = configurationRepository.state
}
