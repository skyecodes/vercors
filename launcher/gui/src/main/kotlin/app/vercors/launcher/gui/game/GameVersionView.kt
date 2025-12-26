package app.vercors.launcher.gui.game

import androidx.compose.runtime.Composable
import app.vercors.launcher.gui.res.Res
import app.vercors.launcher.gui.res.latestRelease
import app.vercors.launcher.gui.res.latestSnapshot
import app.vercors.shared.domain.game.model.GameVersionList
import app.vercors.shared.domain.game.model.GameVersionType
import org.jetbrains.compose.resources.stringResource

fun GameVersionList.convert(): List<GameVersionView> = versions.map {
    GameVersionView(
        id = it.id,
        isLatestRelease = it.id == latestRelease,
        isLatestSnapshot = it.id == latestSnapshot,
        isSnapshot = it.type != GameVersionType.Release
    )
}

data class GameVersionView(
    val id: String,
    val isLatestRelease: Boolean,
    val isLatestSnapshot: Boolean,
    val isSnapshot: Boolean,
) {
    val displayName: String
        @Composable get() = when {
            isLatestRelease -> "$id (${stringResource(Res.string.latestRelease)})"
            isLatestSnapshot -> "$id (${stringResource(Res.string.latestSnapshot)})"
            else -> id
        }
}
