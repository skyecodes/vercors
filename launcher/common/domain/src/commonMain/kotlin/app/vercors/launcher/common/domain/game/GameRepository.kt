package app.vercors.launcher.common.domain.game

import app.vercors.launcher.common.domain.game.model.GameVersionDetails
import app.vercors.launcher.common.domain.game.model.GameVersionList
import app.vercors.shared.domain.Result

interface GameRepository {
    suspend fun getVersionList(): Result<GameVersionList>
    suspend fun getVersionDetails(id: String): Result<GameVersionDetails>
}