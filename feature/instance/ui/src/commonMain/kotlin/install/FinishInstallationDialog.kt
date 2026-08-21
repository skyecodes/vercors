package app.vercors.feature.instance.ui.install

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import app.vercors.core.ui.theme.textStyles
import app.vercors.core.ui.theme.title
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.ManualDownloadItem
import app.vercors.feature.instance.domain.usecase.ObserveManualDownloadItemsUseCase
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.finishInstallation
import app.vercors.feature.instance.ui.manualDownloadsRequired
import app.vercors.feature.instance.ui.openAll
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.copy
import app.vercors.shared.ui.done
import app.vercors.shared.ui.overlay.DialogOverlay
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ClipboardCopy
import com.composables.icons.lucide.ExternalLink
import com.composables.icons.lucide.Lucide
import com.composeunstyled.theme.Theme
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import org.jetbrains.compose.resources.stringResource

/** Shows the modpack files that couldn't be downloaded automatically, live-updating as they get placed. */
class FinishInstallationDialog(
    private val instanceId: InstanceId,
    private val observeManualDownloadItems: ObserveManualDownloadItemsUseCase,
    private val onCopyUrl: (String) -> Unit,
) : DialogOverlay<Unit>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<Unit>,
        transitionController: OverlayTransitionController,
    ) {
        val items by produceState(emptyList<ManualDownloadItem>(), instanceId) {
            observeManualDownloadItems(instanceId).collect { value = it }
        }
        val uriHandler = LocalUriHandler.current

        AppDialog(onClickOutside = { navigator.finish(Unit) }) {
            AppForm {
                AppText(
                    text = stringResource(InstanceRes.string.finishInstallation),
                    style = Theme[textStyles][title]
                )

                AppText(text = stringResource(InstanceRes.string.manualDownloadsRequired))
                items.forEach { item ->
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
                                    .clickableText { onCopyUrl(item.downloadUrl) }
                            )
                        }
                    }
                }
                AppIconTextButton(
                    onClick = { items.filterNot { it.downloaded }.forEach { uriHandler.openUri(it.downloadUrl) } },
                    text = stringResource(InstanceRes.string.openAll),
                    imageVector = Lucide.ExternalLink,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppIconTextButton(
                        onClick = { navigator.finish(Unit) },
                        text = stringResource(SharedRes.string.done),
                        imageVector = Lucide.Check,
                    )
                }
            }
        }
    }
}
