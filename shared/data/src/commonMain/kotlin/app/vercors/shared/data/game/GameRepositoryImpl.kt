package app.vercors.shared.data.game

import app.vercors.meta.client.game.GameApi
import app.vercors.shared.domain.Result
import app.vercors.shared.domain.game.GameRepository
import app.vercors.shared.domain.game.model.GameVersionDetails
import app.vercors.shared.domain.game.model.GameVersionList
import app.vercors.shared.domain.map
import org.koin.core.annotation.Single

@Single
internal class GameRepositoryImpl(
    val gameApi: GameApi
) : GameRepository {
    override suspend fun getVersionList(): Result<GameVersionList> =
        gameApi.getGameVersionList().toResult().map { it.toDomain() }

    override suspend fun getVersionDetails(id: String): Result<GameVersionDetails> =
        gameApi.getGameVersionDetails(id).toResult().map { it.toDomain() }
}
