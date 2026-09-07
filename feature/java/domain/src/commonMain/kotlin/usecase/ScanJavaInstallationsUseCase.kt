package app.vercors.feature.java.domain.usecase

import app.vercors.feature.java.domain.repository.JavaRepository
import dev.zacsweers.metro.Inject

@Inject
class ScanJavaInstallationsUseCase(private val javaRepository: JavaRepository) {
    suspend operator fun invoke() = javaRepository.rescan()
}
