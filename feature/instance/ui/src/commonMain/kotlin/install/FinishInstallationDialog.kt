package app.vercors.feature.instance.ui.install

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppIcon
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.modifier.clickableText
import app.vercors.core.ui.theme.*
import app.vercors.feature.instance.domain.model.ManualDownloadItem
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.finishInstallation
import app.vercors.feature.instance.ui.manualDownloadsCompleted
import app.vercors.shared.ui.*
import app.vercors.shared.ui.overlay.DialogOverlay
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import org.jetbrains.compose.resources.stringResource

class FinishInstallationDialog(
    private val manualDownloadItems: State<List<ManualDownloadItem>>,
    private val onCopyUrl: (String) -> Unit,
) : DialogOverlay<Unit>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<Unit>,
        transitionController: OverlayTransitionController,
    ) {
        val items by manualDownloadItems
        val uriHandler = LocalUriHandler.current

        AppDialog(onClickOutside = { navigator.finish(Unit) }) {
            AppForm(modifier = Modifier.width(IntrinsicSize.Max)) {
                AppText(
                    text = stringResource(InstanceRes.string.finishInstallation),
                    style = Theme[textStyles][title],
                )

                if (items.isNotEmpty()) {
                    AppText(text = stringResource(SharedRes.string.manualDownloadsRequired))
                    items.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
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
                                        .clickableText { onCopyUrl(item.downloadUrl) }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppIconTextButton(
                            onClick = {
                                items.filterNot { it.downloaded }.forEach { uriHandler.openUri(it.downloadUrl) }
                            },
                            text = stringResource(SharedRes.string.openAll),
                            imageVector = Lucide.ExternalLink,
                            backgroundColor = Theme[colors][primary],
                            contentColor = Theme[colors][onPrimary]
                        )

                        AppIconTextButton(
                            onClick = { navigator.finish(Unit) },
                            text = stringResource(SharedRes.string.cancel),
                            imageVector = Lucide.X
                        )

                    }
                } else {
                    AppText(text = stringResource(InstanceRes.string.manualDownloadsCompleted))
                    AppIconTextButton(
                        onClick = { navigator.finish(Unit) },
                        text = stringResource(SharedRes.string.done),
                        imageVector = Lucide.Check,
                        backgroundColor = Theme[colors][primary],
                        contentColor = Theme[colors][onPrimary]
                    )
                }
            }
        }
    }
}
