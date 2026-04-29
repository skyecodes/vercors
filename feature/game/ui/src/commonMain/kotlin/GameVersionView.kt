package app.vercors.feature.game.ui

import androidx.compose.runtime.Composable
import app.vercors.feature.game.domain.model.GameVersionList
import app.vercors.feature.game.domain.model.GameVersionType
import org.jetbrains.compose.resources.stringResource

fun GameVersionList.toView(): List<GameVersionView> = versions.map {
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
            isLatestRelease -> "$id (${stringResource(GameRes.string.latestRelease)})"
            isLatestSnapshot -> "$id (${stringResource(GameRes.string.latestSnapshot)})"
            else -> id
        }
}
