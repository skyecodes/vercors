package app.vercors.feature.game.ui

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppSelectField
import app.vercors.core.ui.component.AppSelectItem
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface
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
    backgroundColor: Color = Theme[colors][surface],
    label: String = stringResource(GameRes.string.loaderVersion)
) {
    val recommendedVersion = remember(versionList) { versionList.firstOrNull { it.stable } }

    AppSelectField(
        label = label,
        value = selectedVersion,
        items = versionList,
        onValueChange = onVersionChange,
        itemRenderer = { value, _ ->
            val isRecommended = value != null && value == recommendedVersion
            AppSelectItem(
                text = value.displayName(isRecommended),
                icon = if (isRecommended) Lucide.Star else null
            )
        },
        backgroundColor = backgroundColor,
        modifier = Modifier.width(300.dp),
    )
}

@Composable
private fun LoaderVersion?.displayName(isRecommended: Boolean): String {
    if (this == null) return ""
    return if (isRecommended) "$version (${stringResource(GameRes.string.recommendedLoaderVersion)})" else version
}
