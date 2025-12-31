package app.vercors.feature.config.gui.domain.usecase

import app.vercors.feature.config.gui.domain.model.GuiConfiguration
import app.vercors.feature.config.gui.domain.repository.GuiConfigurationRepository
import org.koin.core.annotation.Factory

@Factory
class UpdateGuiConfigurationUseCase(
    private val repository: GuiConfigurationRepository
) {
    suspend operator fun invoke(block: (GuiConfiguration) -> GuiConfiguration) = repository.update(block)
}
