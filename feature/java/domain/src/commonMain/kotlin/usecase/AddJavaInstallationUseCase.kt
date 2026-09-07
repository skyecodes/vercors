package app.vercors.feature.java.domain.usecase

import app.vercors.feature.java.domain.model.JavaInstallationError
import app.vercors.feature.java.domain.repository.JavaRepository
import app.vercors.shared.domain.model.JavaInstallation
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class AddJavaInstallationUseCase(private val javaRepository: JavaRepository) {
    context(_: Raise<JavaInstallationError>)
    suspend operator fun invoke(path: String): JavaInstallation = javaRepository.addInstallation(path)
}
