package app.vercors.feature.config.ui.storage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.vercors.core.domain.toHumanReadableSize
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.theme.*
import app.vercors.feature.config.ui.*
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.open
import app.vercors.shared.ui.overlay.ConfirmDialog
import app.vercors.shared.ui.overlay.ConfirmDialogButton
import app.vercors.shared.ui.overlay.ConfirmDialogButtonStyle
import com.composables.icons.lucide.FolderInput
import com.composables.icons.lucide.FolderOpen
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash
import com.composeunstyled.theme.Theme
import com.slack.circuit.overlay.OverlayEffect
import org.jetbrains.compose.resources.stringResource

@Composable
fun StorageConfiguration(
    content: Store<StorageConfigurationState, StorageConfigurationEvent>,
    horizontalTabs: Boolean?,
) {
    val (state, eventSink) = content
    with(eventSink) {
        if (state.promptClearContentCache) {
            OverlayEffect {
                val result = show(
                    ConfirmDialog(
                        messageResource = ConfigRes.string.clearCacheConfirmation,
                        actionButton = ConfirmDialogButton(
                            ConfigRes.string.clearCache,
                            Lucide.Trash,
                            ConfirmDialogButtonStyle.Destructive
                        ),
                    )
                )
                eventSink(StorageConfigurationEvent.OnConfirmClearContentCache(result))
            }
        }

        if (state.promptChangeDataDirectory) {
            OverlayEffect {
                show(state.changeDataDirectoryDialog)
                eventSink(StorageConfigurationEvent.OnChangeDataDirectoryDismiss)
            }
        }

        StorageConfigurationContent(state, horizontalTabs)
    }
}

@Composable
context(eventSink: EventSink<StorageConfigurationEvent>)
private fun StorageConfigurationContent(state: StorageConfigurationState, horizontalTabs: Boolean?) {
    ConfigurationTabScaffold(horizontalTabs) {
        ConfigurationRow(
            title = ConfigRes.string.dataDirectory,
            description = ConfigRes.string.dataDirectoryDesc,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = state.dataDirectory,
                    style = Theme[textStyles][label],
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                AppIconTextButton(
                    onClick = { eventSink(StorageConfigurationEvent.OnOpenDataDirectoryClick) },
                    text = stringResource(SharedRes.string.open),
                    imageVector = Lucide.FolderOpen,
                )
                AppIconTextButton(
                    onClick = { eventSink(StorageConfigurationEvent.OnChangeDataDirectoryClick) },
                    text = stringResource(ConfigRes.string.changeDataDirectory),
                    imageVector = Lucide.FolderInput,
                    enabled = state.canChangeDataDirectory,
                )
            }
        }
        ConfigurationRow(
            title = ConfigRes.string.contentCache,
            description = ConfigRes.string.contentCacheDesc,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.contentCacheSizeBytes?.let {
                    AppText(text = it.toHumanReadableSize())
                }
                AppIconTextButton(
                    onClick = { eventSink(StorageConfigurationEvent.OnClearContentCacheClick) },
                    text = stringResource(ConfigRes.string.clearCache),
                    imageVector = Lucide.Trash,
                    backgroundColor = Theme[colors][error],
                    contentColor = Theme[colors][onError]
                )
            }
        }
    }
}
