package app.vercors.feature.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import app.vercors.core.ui.component.AppLabeledSelectField
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import app.vercors.feature.game.domain.model.LoaderVersion
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoaderVersionSelector(
    selectedVersion: LoaderVersion?,
    versionList: List<LoaderVersion>,
    onVersionChange: (LoaderVersion?) -> Unit,
    backgroundColor: Color = Theme[colors][surface2],
    label: String = stringResource(GameRes.string.loaderVersion)
) {
    val recommendedVersion = remember(versionList) { versionList.firstOrNull { it.stable } }

    AppLabeledSelectField(
        label = label,
        value = selectedVersion,
        items = versionList,
        onValueChange = onVersionChange,
        displayName = { it.displayName(it != null && it == recommendedVersion) },
        badgeIcon = { if (it != null && it == recommendedVersion) Lucide.Star else null },
        backgroundColor = backgroundColor,
    )
}

@Composable
private fun LoaderVersion?.displayName(isRecommended: Boolean): String {
    if (this == null) return ""
    return if (isRecommended) "$version (${stringResource(GameRes.string.recommendedLoaderVersion)})" else version
}
