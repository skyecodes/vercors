package app.vercors.feature.game.domain.usecase

import app.vercors.core.domain.ResourceFlow
import app.vercors.feature.game.domain.model.GameVersionList
import app.vercors.feature.game.domain.repository.GameRepository
import org.koin.core.annotation.Factory

@Factory
class ObserveGameVersionListUseCase(
    private val gameRepository: GameRepository
) {
    operator fun invoke(): ResourceFlow<GameVersionList> = gameRepository.observeVersionManifest()
}
