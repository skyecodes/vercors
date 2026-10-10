package app.vercors.feature.config.domain.usecase

import app.vercors.core.domain.AppConstants
import app.vercors.feature.config.domain.model.UpdateChannel
import app.vercors.feature.config.domain.repository.ConfigurationRepository
import app.vercors.feature.config.domain.service.LauncherUpdateService
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull

@Inject
class CheckForLauncherUpdateUseCase(
    private val launcherUpdateService: LauncherUpdateService,
    private val configurationRepository: ConfigurationRepository,
    @Named(AppConstants.APP_VERSION_KEY) private val appVersion: String,
) {
    suspend operator fun invoke() {
        val channel = configurationRepository.state.now().valueOrNull?.updateChannel
            ?: UpdateChannel.fromVersion(appVersion)
        launcherUpdateService.check(channel)
    }
}
