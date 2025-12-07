package app.vercors.launcher.gui.config.usecase

import app.vercors.launcher.gui.config.GuiConfigurationRepository
import app.vercors.launcher.gui.config.model.GuiConfiguration
import org.koin.core.annotation.Factory

@Factory
class UpdateGuiConfigurationUseCase(
    private val repository: GuiConfigurationRepository
) {
    suspend operator fun invoke(block: (GuiConfiguration) -> GuiConfiguration) = repository.update(block)
}
