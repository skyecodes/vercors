package app.vercors.feature.java.domain.usecase

import app.vercors.feature.config.domain.repository.ConfigurationRepository
import app.vercors.feature.java.domain.repository.JavaRepository
import app.vercors.shared.domain.model.JavaInstallation
import dev.zacsweers.metro.Inject
import opensavvy.state.progressive.ProgressiveOutcome

@Inject
class ResolveJavaInstallationUseCase(
    private val javaRepository: JavaRepository,
    private val configurationRepository: ConfigurationRepository,
) {
    suspend operator fun invoke(majorVersion: Int, instanceOverrideId: String?): JavaInstallation? {
        val installations = (javaRepository.installations.value as? ProgressiveOutcome.Success)?.value.orEmpty()
        val configuration = (configurationRepository.state.value as? ProgressiveOutcome.Success)?.value
        val preferredId = instanceOverrideId ?: configuration?.preferredJavaByMajorVersion?.get(majorVersion)
        return installations.find { it.id == preferredId }
    }
}
