package app.vercors.shared.domain.game.usecase

import app.vercors.shared.domain.Result
import app.vercors.shared.domain.game.GameRepository
import app.vercors.shared.domain.game.model.GameVersionDetails
import org.koin.core.annotation.Factory

@Factory
class GetGameVersionDetailsUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(id: String): Result<GameVersionDetails> = gameRepository.getVersionDetails(id)
}
