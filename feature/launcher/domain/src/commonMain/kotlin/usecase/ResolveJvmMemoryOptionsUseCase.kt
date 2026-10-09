package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.config.domain.repository.ConfigurationRepository
import app.vercors.shared.domain.model.JvmMemoryOptions
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull

@Inject
class ResolveJvmMemoryOptionsUseCase(
    private val configurationRepository: ConfigurationRepository,
) {
    suspend operator fun invoke(instanceOverride: JvmMemoryOptions): JvmMemoryOptions {
        val configuration = configurationRepository.state.now().valueOrNull
        return instanceOverride.withFallback(configuration?.jvmMemoryOptions ?: JvmMemoryOptions())
    }
}
