package app.vercors.launcher.common.domain.game.usecase

import app.vercors.launcher.common.domain.game.GameRepository
import app.vercors.launcher.common.domain.game.model.GameVersionDetails
import app.vercors.shared.domain.Result
import org.koin.core.annotation.Factory

@Factory
class GetGameVersionDetailsUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(id: String): Result<GameVersionDetails> = gameRepository.getVersionDetails(id)
}