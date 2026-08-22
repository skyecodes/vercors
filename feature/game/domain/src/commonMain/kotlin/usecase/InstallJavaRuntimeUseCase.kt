package app.vercors.feature.game.domain.usecase

import app.vercors.feature.game.domain.model.JavaRuntimeError
import app.vercors.feature.game.domain.repository.JavaRuntimeRepository
import app.vercors.shared.domain.model.JavaInstallation
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class InstallJavaRuntimeUseCase(private val javaRuntimeRepository: JavaRuntimeRepository) {
    context(_: Raise<JavaRuntimeError>)
    suspend operator fun invoke(majorVersion: Int): JavaInstallation =
        javaRuntimeRepository.installRuntime(majorVersion)
}
