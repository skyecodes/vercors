package app.vercors.feature.config.ui.nativelibraries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.feature.config.ui.ConfigurationRow
import app.vercors.feature.config.ui.ConfigurationTabScaffold
import app.vercors.shared.ui.*
import app.vercors.shared.ui.component.LibraryPathInputs

@Composable
fun NativeLibraryConfiguration(
    content: Store<NativeLibraryConfigurationState, NativeLibraryConfigurationEvent>,
    horizontalTabs: Boolean?,
) {
    val (state, eventSink) = content
    with(eventSink) {
        NativeLibraryConfigurationContent(state, horizontalTabs)
    }
}

@Composable
context(eventSink: EventSink<NativeLibraryConfigurationEvent>)
private fun NativeLibraryConfigurationContent(state: NativeLibraryConfigurationState, horizontalTabs: Boolean?) {
    ConfigurationTabScaffold(horizontalTabs) {
        ConfigurationRow(title = SharedRes.string.useSystemGlfw, description = SharedRes.string.useSystemGlfwDesc) {
            NativeLibraryConfigurationOverrideRow(
                checked = state.nativeLibraryOptions.useSystemGlfw == true,
                path = state.nativeLibraryOptions.glfwPath.orEmpty(),
                detectedPath = state.detectedGlfwPath,
                onCheckedChange = { eventSink(NativeLibraryConfigurationEvent.OnUseSystemGlfwToggle(it)) },
                onPathChange = { eventSink(NativeLibraryConfigurationEvent.OnGlfwPathChange(it)) },
            )
        }
        ConfigurationRow(title = SharedRes.string.useSystemOpenAl, description = SharedRes.string.useSystemOpenAlDesc) {
            NativeLibraryConfigurationOverrideRow(
                checked = state.nativeLibraryOptions.useSystemOpenAl == true,
                path = state.nativeLibraryOptions.openAlPath.orEmpty(),
                detectedPath = state.detectedOpenAlPath,
                onCheckedChange = { eventSink(NativeLibraryConfigurationEvent.OnUseSystemOpenAlToggle(it)) },
                onPathChange = { eventSink(NativeLibraryConfigurationEvent.OnOpenAlPathChange(it)) },
            )
        }
    }
}

@Composable
private fun RowScope.NativeLibraryConfigurationOverrideRow(
    checked: Boolean,
    path: String,
    detectedPath: String?,
    onCheckedChange: (Boolean) -> Unit,
    onPathChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LibraryPathInputs(checked, path, detectedPath, onCheckedChange, onPathChange)
    }
}
