package app.vercors.feature.game.domain.repository

import app.vercors.core.domain.ResourceFlow
import app.vercors.feature.game.domain.model.GameVersionList

interface GameRepository {
    fun observeVersionManifest(): ResourceFlow<GameVersionList>
}
