package app.vercors.feature.game.domain.usecase

import app.vercors.feature.game.domain.repository.JavaRepository
import dev.zacsweers.metro.Inject

@Inject
class ScanJavaInstallationsUseCase(private val javaRepository: JavaRepository) {
    suspend operator fun invoke() = javaRepository.rescan()
}
