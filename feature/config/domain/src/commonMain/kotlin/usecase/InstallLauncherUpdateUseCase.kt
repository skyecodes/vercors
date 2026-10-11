package app.vercors.feature.config.domain.usecase

import app.vercors.feature.config.domain.service.LauncherUpdateService
import dev.zacsweers.metro.Inject

@Inject
class InstallLauncherUpdateUseCase(
    private val launcherUpdateService: LauncherUpdateService
) {
    suspend operator fun invoke(): Unit = launcherUpdateService.installAndRestart()
}
