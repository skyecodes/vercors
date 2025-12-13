package app.vercors.shared.domain.game

import app.vercors.shared.domain.Result
import app.vercors.shared.domain.game.model.GameVersionDetails
import app.vercors.shared.domain.game.model.GameVersionList

interface GameRepository {
    suspend fun getVersionList(): Result<GameVersionList>
    suspend fun getVersionDetails(id: String): Result<GameVersionDetails>
}
