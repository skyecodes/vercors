package app.vercors.feature.project.ui.install

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.modifier.clickableText
import app.vercors.core.ui.theme.*
import app.vercors.feature.project.ui.*
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.cancel
import app.vercors.shared.ui.copy
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun InstallProject(
    store: Store<InstallProjectState, InstallProjectEvent>,
) {
    val (state, eventSink) = store

    AppDialog(onClickOutside = { eventSink(InstallProjectEvent.OnCancelClick) }) {
        AppForm {
            AppText(
                text = stringResource(ProjectRes.string.installToInstance),
                style = Theme[textStyles][title]
            )

            AppSelectField(
                label = stringResource(ProjectRes.string.instance),
                value = state.selectedInstance,
                items = state.instanceList,
                onValueChange = { eventSink(InstallProjectEvent.OnInstanceSelected(it?.id)) },
                itemRenderer = { value, _ -> AppSelectItem(text = value?.name ?: "") },
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.width(300.dp),
            )

            if (state.instanceList.isEmpty()) {
                AppText(text = stringResource(ProjectRes.string.noCompatibleInstances))
            }

            if (state.selectedInstance != null) {
                AppSelectField(
                    label = stringResource(ProjectRes.string.`file`),
                    value = state.selectedFile,
                    items = state.fileList,
                    onValueChange = { eventSink(InstallProjectEvent.OnFileSelected(it?.id)) },
                    itemRenderer = { value, _ -> AppSelectItem(text = value?.displayName ?: "") },
                    backgroundColor = Theme[colors][surface2],
                    modifier = Modifier.width(300.dp),
                )
            }

            if (state.dependencies.isNotEmpty()) {
                AppText(text = stringResource(ProjectRes.string.requiredDependencies))
                state.dependencies.forEach { dependency ->
                    AppCheckbox(
                        label = dependency.name,
                        checked = dependency.projectId in state.selectedDependencyIds,
                        onCheckedChange = {
                            eventSink(InstallProjectEvent.OnDependencyToggled(dependency.projectId, it))
                        }
                    )
                }
            }

            if (state.manualDownloadItems.isNotEmpty()) {
                val uriHandler = LocalUriHandler.current
                AppText(text = stringResource(ProjectRes.string.manualDownloadsRequired))
                state.manualDownloadItems.forEach { item ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(text = item.fileName, modifier = Modifier.weight(1f))
                        if (item.downloaded) {
                            AppIcon(
                                imageVector = Lucide.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            AppIcon(
                                imageVector = Lucide.ClipboardCopy,
                                contentDescription = stringResource(SharedRes.string.copy),
                                modifier = Modifier.size(16.dp)
                                    .clickableText { eventSink(InstallProjectEvent.OnCopyUrlClick(item.downloadUrl)) }
                            )
                        }
                    }
                }
                AppIconTextButton(
                    onClick = { state.manualDownloadItems.forEach { uriHandler.openUri(it.downloadUrl) } },
                    text = stringResource(ProjectRes.string.openAll),
                    imageVector = Lucide.ExternalLink,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppIconTextButton(
                    onClick = { eventSink(InstallProjectEvent.OnCancelClick) },
                    text = stringResource(SharedRes.string.cancel),
                    imageVector = Lucide.X,
                )
                AppIconTextButton(
                    onClick = { eventSink(InstallProjectEvent.OnConfirmClick) },
                    text = stringResource(ProjectRes.string.install),
                    imageVector = Lucide.Download,
                    backgroundColor = Theme[colors][primary],
                    contentColor = Theme[colors][onPrimary],
                    enabled = state.canInstall
                )
            }
        }
    }
}
