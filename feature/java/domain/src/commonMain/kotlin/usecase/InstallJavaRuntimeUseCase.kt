package app.vercors.feature.java.domain.usecase

import app.vercors.feature.java.domain.model.JavaRuntimeError
import app.vercors.feature.java.domain.repository.JavaRuntimeRepository
import app.vercors.shared.domain.model.JavaInstallation
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class InstallJavaRuntimeUseCase(
    private val javaRuntimeRepository: JavaRuntimeRepository
) {
    context(_: Raise<JavaRuntimeError>)
    suspend operator fun invoke(majorVersion: Int): JavaInstallation =
        javaRuntimeRepository.installRuntime(majorVersion)
}
