package app.vercors.feature.game.domain.usecase

import app.vercors.feature.game.domain.repository.JavaRepository
import app.vercors.shared.domain.model.JavaInstallation
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveJavaInstallationsUseCase(private val javaRepository: JavaRepository) {
    operator fun invoke(): ProgressiveFlow<Nothing, List<JavaInstallation>> = javaRepository.installations
}
