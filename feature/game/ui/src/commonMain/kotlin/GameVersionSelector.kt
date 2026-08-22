package app.vercors.feature.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppCheckbox
import app.vercors.core.ui.component.AppSelectField
import app.vercors.core.ui.component.AppSelectItem
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import com.composables.icons.lucide.Bug
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameVersionSelector(
    selectedVersion: GameVersionView?,
    versionList: List<GameVersionView>,
    onVersionChange: (GameVersionView?) -> Unit,
    backgroundColor: Color = Theme[colors][surface2],
    label: String = stringResource(GameRes.string.gameVersion)
) {
    var versionSearch by remember { mutableStateOf("") }
    var showSnapshot by remember { mutableStateOf(true) }

    val filteredVersionList = remember(versionList, versionSearch, showSnapshot) {
        versionList.filter {
            (versionSearch.isBlank() || it.id.contains(
                versionSearch,
                ignoreCase = true
            )) && (showSnapshot || !it.isSnapshot)
        }
    }

    val filteredSelectedVersion = remember(filteredVersionList, selectedVersion) {
        filteredVersionList.find { it == selectedVersion }
            ?: filteredVersionList.firstOrNull { it.isLatestRelease }
            ?: filteredVersionList.firstOrNull()
    }

    LaunchedEffect(filteredSelectedVersion) {
        if (filteredSelectedVersion != selectedVersion) {
            onVersionChange(filteredSelectedVersion)
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        AppSelectField(
            label = label,
            value = filteredSelectedVersion,
            items = filteredVersionList,
            onValueChange = onVersionChange,
            itemRenderer = { value, _ ->
                AppSelectItem(
                    text = value?.displayName ?: "",
                    icon = if (value?.isLatestRelease == true) Lucide.Star else if (value?.isLatestSnapshot == true) Lucide.Bug else null
                )
            },
            searchable = true,
            search = versionSearch,
            onSearchChange = { versionSearch = it },
            backgroundColor = backgroundColor,
            modifier = Modifier.width(300.dp),
        )

        AppCheckbox(
            modifier = Modifier.padding(bottom = 4.dp),
            label = stringResource(GameRes.string.showSnapshots),
            checked = showSnapshot,
            onCheckedChange = { showSnapshot = it },
            uncheckedBackgroundColor = backgroundColor,
        )
    }
}
