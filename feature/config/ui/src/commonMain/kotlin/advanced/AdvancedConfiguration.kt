package app.vercors.feature.config.ui.advanced

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.vercors.core.ui.component.AppNumberField
import app.vercors.core.ui.component.AppTextField
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import app.vercors.feature.config.ui.*
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.Store
import com.composeunstyled.theme.Theme

@Composable
fun AdvancedConfiguration(
    content: Store<AdvancedConfigurationState, AdvancedConfigurationEvent>,
    horizontalTabs: Boolean?,
) {
    val (state, eventSink) = content
    with(eventSink) {
        AdvancedConfigurationContent(state, horizontalTabs)
    }
}

@Composable
context(eventSink: EventSink<AdvancedConfigurationEvent>)
private fun AdvancedConfigurationContent(state: AdvancedConfigurationState, horizontalTabs: Boolean?) {
    ConfigurationTabScaffold(horizontalTabs) {
        ConfigurationRow(
            title = ConfigRes.string.metaApiUrl,
            description = ConfigRes.string.metaApiUrlDesc,
            requiresRestart = true,
        ) {
            AppTextField(
                initialValue = state.metaApiUrl.orEmpty(),
                onValueChange = { eventSink(AdvancedConfigurationEvent.OnMetaApiUrlChange(it)) },
                debounceMillis = 500,
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.weight(1f)
            )
        }
        if (state.networkTimeoutSeconds != null) {
            ConfigurationRow(
                title = ConfigRes.string.networkTimeout,
                description = ConfigRes.string.networkTimeoutDesc,
                requiresRestart = true,
            ) {
                AppNumberField(
                    value = state.networkTimeoutSeconds,
                    onValueChange = { eventSink(AdvancedConfigurationEvent.OnNetworkTimeoutChange(it)) },
                    valueRange = 1..120,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.parallelDownloads != null) {
            ConfigurationRow(
                title = ConfigRes.string.parallelDownloads,
                description = ConfigRes.string.parallelDownloadsDesc,
            ) {
                AppNumberField(
                    value = state.parallelDownloads,
                    onValueChange = { eventSink(AdvancedConfigurationEvent.OnParallelDownloadsChange(it)) },
                    valueRange = 1..64,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.parallelFilesystemOperations != null) {
            ConfigurationRow(
                title = ConfigRes.string.parallelFilesystemOperations,
                description = ConfigRes.string.parallelFilesystemOperationsDesc,
            ) {
                AppNumberField(
                    value = state.parallelFilesystemOperations,
                    onValueChange = { eventSink(AdvancedConfigurationEvent.OnParallelFilesystemOperationsChange(it)) },
                    valueRange = 1..64,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
