package app.vercors.feature.config.gui.domain.usecase

import app.vercors.core.domain.ResourceStateFlow
import app.vercors.feature.config.gui.domain.model.GuiConfiguration
import app.vercors.feature.config.gui.domain.repository.GuiConfigurationRepository
import dev.zacsweers.metro.Inject

@Inject
class ObserveGuiConfigurationUseCase(
    private val guiConfigurationRepository: GuiConfigurationRepository
) {
    operator fun invoke(): ResourceStateFlow<GuiConfiguration> = guiConfigurationRepository.state
}
