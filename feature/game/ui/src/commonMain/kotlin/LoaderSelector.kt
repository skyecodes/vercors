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
import app.vercors.feature.game.domain.model.ModLoaderType
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoaderSelector(
    selectedLoader: ModLoaderType?,
    availableLoaders: List<ModLoaderType>,
    onLoaderChange: (ModLoaderType?) -> Unit,
    backgroundColor: Color = Theme[colors][surface],
    label: String = stringResource(GameRes.string.loader)
) {
    val items = remember(availableLoaders) { listOf(null) + availableLoaders }

    AppSelectField(
        label = label,
        value = selectedLoader,
        items = items,
        onValueChange = onLoaderChange,
        itemRenderer = { value, _ -> AppSelectItem(text = value.displayName, icon = value?.icon) },
        backgroundColor = backgroundColor,
        modifier = Modifier.width(300.dp),
    )
}
