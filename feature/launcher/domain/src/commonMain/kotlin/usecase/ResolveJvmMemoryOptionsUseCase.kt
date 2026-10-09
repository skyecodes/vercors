package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.config.domain.repository.ConfigurationRepository
import app.vercors.shared.domain.model.JvmMemoryOptions
import dev.zacsweers.metro.Inject
import opensavvy.state.progressive.ProgressiveOutcome

@Inject
class ResolveJvmMemoryOptionsUseCase(
    private val configurationRepository: ConfigurationRepository,
) {
    operator fun invoke(instanceOverride: JvmMemoryOptions): JvmMemoryOptions {
        val configuration = (configurationRepository.state.value as? ProgressiveOutcome.Success)?.value
        return instanceOverride.withFallback(configuration?.jvmMemoryOptions ?: JvmMemoryOptions())
    }
}
