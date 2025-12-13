package app.vercors.shared.domain.game.usecase

import app.vercors.shared.domain.Result
import app.vercors.shared.domain.game.GameRepository
import app.vercors.shared.domain.game.model.GameVersionList
import org.koin.core.annotation.Factory

@Factory
class GetGameVersionListUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(): Result<GameVersionList> = gameRepository.getVersionList()
}
