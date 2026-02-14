package app.vercors.feature.game.domain.repository

import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.model.GameVersionList
import arrow.core.raise.Raise
import opensavvy.state.coroutines.ProgressiveFlow

interface GameRepository {
    fun observeVersionManifest(): ProgressiveFlow<GameVersionError.InvalidManifest, GameVersionList>

    context(_: Raise<GameVersionError>)
    suspend fun getVersionDetails(versionId: String): GameVersionDetails

    context(_: Raise<GameVersionError.InvalidAssetIndex>)
    suspend fun getAssetIndex(assetIndex: GameVersionDetails.AssetIndex): GameAssetIndex
}
