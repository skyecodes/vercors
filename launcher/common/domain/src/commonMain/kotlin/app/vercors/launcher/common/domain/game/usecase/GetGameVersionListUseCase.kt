package app.vercors.launcher.common.domain.game.usecase

import app.vercors.launcher.common.domain.game.GameRepository
import app.vercors.launcher.common.domain.game.model.GameVersionList
import app.vercors.shared.domain.Result
import org.koin.core.annotation.Factory

@Factory
class GetGameVersionListUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(): Result<GameVersionList> = gameRepository.getVersionList()
}