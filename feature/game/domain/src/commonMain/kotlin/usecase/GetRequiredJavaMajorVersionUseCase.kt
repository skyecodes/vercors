package app.vercors.feature.game.domain.usecase

import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.repository.GameRepository
import arrow.core.raise.recover
import dev.zacsweers.metro.Inject

/** Best-effort lookup of the Java major version a game version requires, for UI display only (e.g. showing
 *  which installation an instance's "inherit from global" Java choice currently resolves to). Swallows
 *  [GameVersionError] and returns null rather than propagating — callers must treat null as "unknown". */
@Inject
class GetRequiredJavaMajorVersionUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(gameVersion: String): Int? =
        recover({ gameRepository.getVersionDetails(gameVersion).javaVersion?.majorVersion }) { _: GameVersionError -> null }
}
