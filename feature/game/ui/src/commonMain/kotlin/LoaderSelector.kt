package app.vercors.feature.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import app.vercors.core.ui.component.AppLabeledSelectField
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import app.vercors.feature.game.domain.model.ModLoaderType
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoaderSelector(
    selectedLoader: ModLoaderType?,
    availableLoaders: List<ModLoaderType>,
    onLoaderChange: (ModLoaderType?) -> Unit,
    backgroundColor: Color = Theme[colors][surface2],
    label: String = stringResource(GameRes.string.loader)
) {
    val items = remember(availableLoaders) { listOf(null) + availableLoaders }

    AppLabeledSelectField(
        label = label,
        value = selectedLoader,
        items = items,
        onValueChange = onLoaderChange,
        displayName = { it.displayName },
        badgeIcon = { it?.icon },
        backgroundColor = backgroundColor,
    )
}
