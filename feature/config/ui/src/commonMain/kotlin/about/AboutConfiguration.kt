package app.vercors.feature.config.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import app.vercors.core.domain.AppConstants
import app.vercors.core.ui.component.*
import app.vercors.core.ui.modifier.clickableText
import app.vercors.core.ui.theme.*
import app.vercors.feature.config.domain.model.LauncherUpdateState
import app.vercors.feature.config.domain.model.UpdateChannel
import app.vercors.feature.config.ui.*
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.logo
import app.vercors.shared.ui.overlay.ConfirmDialog
import app.vercors.shared.ui.overlay.ConfirmDialogButton
import app.vercors.shared.ui.overlay.ConfirmDialogButtonStyle
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.overlay.OverlayEffect
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun AboutConfiguration(
    content: Store<AboutConfigurationState, AboutConfigurationEvent>,
    horizontalTabs: Boolean?,
) {
    val (state, eventSink) = content
    with(eventSink) {
        if (state.promptRestartForUpdate) {
            OverlayEffect {
                val result = show(
                    ConfirmDialog(
                        messageResource = ConfigRes.string.restartForUpdateConfirmation,
                        actionButton = ConfirmDialogButton(
                            ConfigRes.string.restartToUpdate,
                            Lucide.RefreshCw,
                            ConfirmDialogButtonStyle.Primary
                        ),
                    )
                )
                eventSink(AboutConfigurationEvent.OnConfirmRestartForUpdate(result))
            }
        }

        AboutConfigurationContent(state, horizontalTabs)
    }
}

@Composable
context(eventSink: EventSink<AboutConfigurationEvent>)
private fun AboutConfigurationContent(state: AboutConfigurationState, horizontalTabs: Boolean?) {
    val uriHandler = LocalUriHandler.current
    ConfigurationTabScaffold(horizontalTabs) {
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            AppIcon(
                imageVector = vectorResource(SharedRes.drawable.logo),
                contentDescription = null,
                tint = Theme[colors][primary],
                modifier = Modifier.size(128.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                modifier = Modifier.fillMaxHeight()
            ) {
                AppText(
                    text = "${state.appName} v${state.appVersion}",
                    style = Theme[textStyles][title]
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    itemVerticalAlignment = Alignment.CenterVertically
                ) {
                    AboutConfigurationUpdateActions(state.launcherUpdate)
                    AboutConfigurationUpdateStatus(state.launcherUpdate) { uriHandler.openUri(it) }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppIconTextButton(
                        imageVector = Lucide.House,
                        text = stringResource(ConfigRes.string.homepage),
                        onClick = { uriHandler.openUri(AppConstants.HOMEPAGE_URL) }
                    )
                    AppIconTextButton(
                        imageVector = Lucide.Code,
                        text = stringResource(ConfigRes.string.repository),
                        onClick = { uriHandler.openUri(AppConstants.REPOSITORY_URL) }
                    )
                    AppIconTextButton(
                        imageVector = Lucide.Bug,
                        text = stringResource(ConfigRes.string.issueTracker),
                        onClick = { uriHandler.openUri(AppConstants.ISSUES_URL) }
                    )
                }
            }
        }
        if (state.checkForUpdatesOnStart != null) {
            ConfigurationRow(
                title = ConfigRes.string.checkForUpdatesOnStart,
                description = ConfigRes.string.checkForUpdatesOnStartDesc,
            ) {
                AppCheckbox(
                    checked = state.checkForUpdatesOnStart,
                    onCheckedChange = { eventSink(AboutConfigurationEvent.OnCheckForUpdatesOnStartToggle) }
                )
            }
        }
        if (state.updateChannel != null) {
            ConfigurationRow(
                title = ConfigRes.string.updateChannel,
                description = ConfigRes.string.updateChannelDesc,
            ) {
                AppSelectField(
                    value = state.updateChannel,
                    items = UpdateChannel.entries,
                    onValueChange = { eventSink(AboutConfigurationEvent.OnUpdateChannelChange(it)) },
                    itemRenderer = { channel, _ ->
                        AppText(text = stringResource(channel.label))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AboutConfigurationUpdateStatus(update: LauncherUpdateState, onOpen: (String) -> Unit) {
    val labelStyle = Theme[textStyles][label]
    when (update) {
        LauncherUpdateState.Idle -> AppText(text = stringResource(ConfigRes.string.updatesDesc), style = labelStyle)
        LauncherUpdateState.Checking ->
            AppText(text = stringResource(ConfigRes.string.checkingForUpdates), style = labelStyle)

        LauncherUpdateState.UpToDate -> AppText(
            text = stringResource(ConfigRes.string.upToDate),
            style = labelStyle,
            color = Theme[colors][success]
        )

        is LauncherUpdateState.Failed -> AppText(
            text = stringResource(ConfigRes.string.updateFailed, update.error.message),
            style = labelStyle,
            color = Theme[colors][error]
        )

        is LauncherUpdateState.Restarting -> AppText(
            text = stringResource(ConfigRes.string.restartingToUpdate, update.version),
            style = labelStyle
        )

        is LauncherUpdateState.Found -> AboutConfigurationAvailableUpdate(update.version, update.releaseUrl, onOpen)
    }
}

@Composable
private fun AboutConfigurationAvailableUpdate(version: String, releaseUrl: String, onOpen: (String) -> Unit) {
    AppText(
        text = stringResource(ConfigRes.string.updateAvailable, version),
        style = Theme[textStyles][label],
        color = Theme[colors][warn]
    )
    AppText(
        text = stringResource(ConfigRes.string.viewRelease),
        style = Theme[textStyles][label],
        color = Theme[colors][primary],
        modifier = Modifier.clickableText { onOpen(releaseUrl) }
    )
}

@Composable
context(eventSink: EventSink<AboutConfigurationEvent>)
private fun RowScope.AboutConfigurationUpdateActions(update: LauncherUpdateState) {
    when (update) {
        is LauncherUpdateState.Downloading -> {
            AppDownloadProgress(
                progress = update.download?.normalized?.toFloat() ?: 0f,
                download = update.download,
                modifier = Modifier.width(300.dp)
            )
        }

        is LauncherUpdateState.ReadyToInstall, is LauncherUpdateState.Restarting -> AppIconTextButton(
            onClick = { eventSink(AboutConfigurationEvent.OnRestartToUpdateClick) },
            text = stringResource(
                if (update is LauncherUpdateState.Restarting) ConfigRes.string.restarting
                else ConfigRes.string.restartToUpdate
            ),
            imageVector = Lucide.RefreshCw,
            enabled = update is LauncherUpdateState.ReadyToInstall,
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary]
        )

        is LauncherUpdateState.Available -> {
            AboutConfigurationCheckForUpdatesButton(enabled = true)
            if (update.canInstall) {
                AppIconTextButton(
                    onClick = { eventSink(AboutConfigurationEvent.OnUpdateClick) },
                    text = stringResource(ConfigRes.string.update),
                    imageVector = Lucide.Download,
                    backgroundColor = Theme[colors][primary],
                    contentColor = Theme[colors][onPrimary]
                )
            }
        }

        else -> AboutConfigurationCheckForUpdatesButton(enabled = update != LauncherUpdateState.Checking)
    }
}

@Composable
context(eventSink: EventSink<AboutConfigurationEvent>)
private fun AboutConfigurationCheckForUpdatesButton(enabled: Boolean) {
    AppIconTextButton(
        onClick = { eventSink(AboutConfigurationEvent.OnCheckForUpdatesClick) },
        text = stringResource(ConfigRes.string.checkForUpdates),
        imageVector = Lucide.RefreshCw,
        enabled = enabled,
    )
}

private val UpdateChannel.label: StringResource
    get() = when (this) {
        UpdateChannel.Stable -> ConfigRes.string.updateChannelStable
        UpdateChannel.Beta -> ConfigRes.string.updateChannelBeta
        UpdateChannel.Alpha -> ConfigRes.string.updateChannelAlpha
    }
