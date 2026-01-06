package app.vercors.feature.game.domain.repository

import app.vercors.core.domain.ResourceFlow
import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.GameVersionList

interface GameRepository {
    fun observeVersionManifest(): ResourceFlow<GameVersionList>
    suspend fun getVersionDetails(versionId: String): GameVersionDetails
    suspend fun getAssetIndex(assetIndex: GameVersionDetails.AssetIndex): GameAssetIndex
}
