package app.vercors.launcher.common.data.game

import app.vercors.launcher.common.domain.game.model.GameVersionDetails
import app.vercors.launcher.common.domain.game.model.GameVersionList
import app.vercors.launcher.common.domain.game.model.GameVersionType
import app.vercors.meta.lib.game.GameVersionDetailsData
import app.vercors.meta.lib.game.GameVersionListData
import app.vercors.meta.lib.game.GameVersionTypeData

internal fun GameVersionListData.convert(): GameVersionList = GameVersionList(
    latestRelease = latestRelease,
    latestSnapshot = latestSnapshot,
    versions = versions.map {
        GameVersionList.Version(
            id = it.id,
            type = it.type.convert()
        )
    }
)

internal fun GameVersionTypeData.convert(): GameVersionType = when (this) {
    GameVersionTypeData.Release -> GameVersionType.Release
    GameVersionTypeData.Snapshot -> GameVersionType.Snapshot
    GameVersionTypeData.Beta -> GameVersionType.Beta
    GameVersionTypeData.Alpha -> GameVersionType.Alpha
}

internal fun GameVersionDetailsData.convert(): GameVersionDetails = GameVersionDetails(
    id = id
)