package app.vercors.feature.game.domain.usecase

import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.repository.GameRepository
import arrow.core.raise.recover
import dev.zacsweers.metro.Inject

@Inject
class GetRequiredJavaMajorVersionUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(gameVersion: String): Int? =
        recover({ gameRepository.getVersionDetails(gameVersion).javaVersion?.majorVersion }) { _: GameVersionError -> null }
}
