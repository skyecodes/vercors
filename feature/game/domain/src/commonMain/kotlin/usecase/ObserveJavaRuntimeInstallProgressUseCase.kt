package app.vercors.feature.game.domain.usecase

import app.vercors.feature.game.domain.model.JavaRuntimeInstallStatus
import app.vercors.feature.game.domain.repository.JavaRuntimeRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.StateFlow

@Inject
class ObserveJavaRuntimeInstallProgressUseCase(private val javaRuntimeRepository: JavaRuntimeRepository) {
    operator fun invoke(): StateFlow<Map<Int, JavaRuntimeInstallStatus>> = javaRuntimeRepository.installProgress
}
