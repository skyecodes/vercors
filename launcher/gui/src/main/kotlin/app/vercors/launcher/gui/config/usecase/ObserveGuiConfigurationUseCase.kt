package app.vercors.launcher.gui.config.usecase

import app.vercors.launcher.gui.config.GuiConfigurationRepository
import app.vercors.launcher.gui.config.model.GuiConfiguration
import app.vercors.shared.domain.ResourceStateFlow
import org.koin.core.annotation.Factory

@Factory
class ObserveGuiConfigurationUseCase(
    private val guiConfigurationRepository: GuiConfigurationRepository
) {
    operator fun invoke(): ResourceStateFlow<GuiConfiguration> = guiConfigurationRepository.state
}