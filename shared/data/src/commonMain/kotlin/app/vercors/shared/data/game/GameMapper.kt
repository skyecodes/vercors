package app.vercors.shared.data.game

import app.vercors.meta.lib.game.GameVersionDetailsData
import app.vercors.meta.lib.game.GameVersionListData
import app.vercors.meta.lib.game.GameVersionTypeData
import app.vercors.shared.domain.game.model.BriefGameVersion
import app.vercors.shared.domain.game.model.GameVersionDetails
import app.vercors.shared.domain.game.model.GameVersionList
import app.vercors.shared.domain.game.model.GameVersionType

internal fun GameVersionListData.toDomain(): GameVersionList = GameVersionList(
    latestRelease = latestRelease,
    latestSnapshot = latestSnapshot,
    versions = versions.map {
        BriefGameVersion(
            id = it.id,
            type = it.type.toDomain()
        )
    }
)

internal fun GameVersionTypeData.toDomain(): GameVersionType = when (this) {
    GameVersionTypeData.Release -> GameVersionType.Release
    GameVersionTypeData.Snapshot -> GameVersionType.Snapshot
    GameVersionTypeData.Beta -> GameVersionType.Beta
    GameVersionTypeData.Alpha -> GameVersionType.Alpha
}

internal fun GameVersionDetailsData.toDomain(): GameVersionDetails = GameVersionDetails(
    id = id
)
