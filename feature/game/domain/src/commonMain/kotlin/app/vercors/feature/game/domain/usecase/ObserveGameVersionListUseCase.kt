package app.vercors.feature.game.domain.usecase

import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.model.GameVersionList
import app.vercors.feature.game.domain.repository.GameRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveGameVersionListUseCase(
    private val gameRepository: GameRepository
) {
    operator fun invoke(): ProgressiveFlow<GameVersionError.InvalidManifest, GameVersionList> =
        gameRepository.observeVersionManifest()
}
