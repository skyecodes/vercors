package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.config.domain.repository.ConfigurationRepository
import app.vercors.shared.domain.model.JvmMemoryOptions
import dev.zacsweers.metro.Inject
import opensavvy.state.progressive.ProgressiveOutcome

/** Resolves the effective JVM memory options: the instance's own override if it has one, otherwise the
 *  globally-configured default. */
@Inject
class ResolveJvmMemoryOptionsUseCase(
    private val configurationRepository: ConfigurationRepository,
) {
    suspend operator fun invoke(instanceOverride: JvmMemoryOptions?): JvmMemoryOptions {
        if (instanceOverride != null) return instanceOverride
        val configuration = (configurationRepository.state.value as? ProgressiveOutcome.Success)?.value
        return configuration?.jvmMemoryOptions ?: JvmMemoryOptions()
    }
}
