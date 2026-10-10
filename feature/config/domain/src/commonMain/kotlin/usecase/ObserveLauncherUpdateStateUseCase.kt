package app.vercors.feature.config.domain.usecase

import app.vercors.feature.config.domain.model.LauncherUpdateState
import app.vercors.feature.config.domain.service.LauncherUpdateService
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.StateFlow

@Inject
class ObserveLauncherUpdateStateUseCase(
    private val launcherUpdateService: LauncherUpdateService
) {
    operator fun invoke(): StateFlow<LauncherUpdateState> = launcherUpdateService.state
}
