package app.vercors.feature.instance.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.AppAnimatedContent
import app.vercors.core.ui.animation.appSharedElement
import app.vercors.core.ui.component.*
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.ui.actions.InstanceActionsEvent
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.delete
import app.vercors.shared.ui.openDetails
import com.composables.icons.lucide.*
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.theme.Theme
import com.slack.circuit.sharedelements.SharedElementTransitionScope
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BriefInstanceCard(
    instance: BriefInstanceView?,
    onEvent: (InstanceActionsEvent) -> Unit,
    modifier: Modifier,
    origin: InstanceSharedElementOrigin
) = SharedElementTransitionScope {
    if (instance != null) {
        val interactionSource = remember { MutableInteractionSource() }

        BriefInstanceCardLayout(
            modifier = modifier.hoverable(interactionSource = interactionSource)
                .instanceSharedElement(instance.id, "card", origin),
            interactionSource = interactionSource,
            onCardClick = { onEvent(InstanceActionsEvent.OnInstanceClick(instance.id)) },
            forceShowAction = instance.status !is InstanceStatus.Idle && instance.status !is InstanceStatus.Failed,
            contextMenuItems = instanceContextMenuItems(
                instance = instance,
                onEvent = onEvent,
            ),
            icon = {
                InstanceIcon(
                    instanceId = instance.id,
                    name = instance.name,
                    iconFilePath = instance.iconFilePath,
                    updatedAt = instance.updatedAt,
                    status = instance.status,
                    size = 36.dp,
                    shape = RoundedCornerShape(6.dp),
                    origin = origin,
                )
            },
            title = {
                AppText(
                    text = instance.name,
                    fontWeight = FontWeight.SemiBold,
                    minLines = 2,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp).instanceSharedElement(instance.id, "title", origin),
                )
            },
            type = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppText(
                        text = instance.type,
                        style = Theme[textStyles][label],
                        modifier = Modifier.instanceSharedElement(instance.id, "type", origin),
                    )
                    if (instance.updateAvailable) {
                        ProvideContentColor(Theme[colors][primary]) {
                            AppIcon(
                                imageVector = Lucide.CircleArrowUp,
                                modifier = Modifier.size(14.dp),
                                contentDescription = stringResource(InstanceRes.string.updateAvailableBadge),
                            )
                            AppText(
                                text = stringResource(InstanceRes.string.updateAvailableBadge),
                                style = Theme[textStyles][label],
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            },
            loaderVersion = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    AppText(
                        text = instance.loader,
                        style = Theme[textStyles][label],
                        modifier = Modifier.instanceSharedElement(instance.id, "loader", origin),
                    )
                    AppText(
                        text = instance.version,
                        style = Theme[textStyles][label],
                        modifier = Modifier.instanceSharedElement(instance.id, "version", origin),
                    )
                }
            },
            actionArea = {
                when (instance.status) {
                    is InstanceStatus.Idle, is InstanceStatus.Failed ->
                        InstanceLaunchButton(
                            onClick = { onEvent(InstanceActionsEvent.OnInstanceLaunchClick(instance.id)) }
                        )

                    is InstanceStatus.Launching, is InstanceStatus.Installing -> AppProgressIndicator(
                        progress = instance.status.progress ?: 0f,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )

                    is InstanceStatus.Running ->
                        InstanceStopButton(onClick = { onEvent(InstanceActionsEvent.OnInstanceStopClick(instance.id)) })

                    is InstanceStatus.Stopping ->
                        InstanceKillButton(onClick = { onEvent(InstanceActionsEvent.OnInstanceKillClick(instance.id)) })
                }
            }
        )
    } else {
        BriefInstanceCardLayout(
            modifier = modifier,
            icon = { AppLoadingPlaceholder(Modifier.size(36.dp)) },
            title = { AppLoadingTextPlaceholder(Modifier.width(100.dp)) },
            type = { AppLoadingTextPlaceholder(Modifier.width(50.dp)) },
            loaderVersion = { AppLoadingTextPlaceholder(Modifier.width(50.dp)) },
            actionArea = {}
        )
    }
}

@Composable
private fun instanceContextMenuItems(
    instance: BriefInstanceView,
    onEvent: (InstanceActionsEvent) -> Unit,
): () -> List<AppContextMenuItem> {
    val launchLabel = stringResource(InstanceRes.string.launch)
    val openDetailsLabel = stringResource(SharedRes.string.openDetails)
    val openInstanceFolderLabel = stringResource(InstanceRes.string.openInstanceFolder)
    val openProjectLabel = instance.modpackInfo?.let {
        stringResource(InstanceRes.string.openProject, it.platformId.toPlatformDisplayName())
    }
    val copyLabel = stringResource(InstanceRes.string.copyInstance)
    val exportLabel = stringResource(InstanceRes.string.exportInstance)
    val deleteLabel = stringResource(SharedRes.string.delete)
    val errorColor = Theme[colors][error]
    val successColor = Theme[colors][success]

    return {
        listOfNotNull(
            if (instance.status is InstanceStatus.Idle || instance.status is InstanceStatus.Failed) {
                AppContextMenuItem(
                    label = launchLabel,
                    imageVector = Lucide.Play,
                    contentColor = successColor,
                    onClick = { onEvent(InstanceActionsEvent.OnInstanceLaunchClick(instance.id)) },
                )
            } else null,
            AppContextMenuItem(
                label = openDetailsLabel,
                imageVector = Lucide.Info,
                onClick = { onEvent(InstanceActionsEvent.OnInstanceClick(instance.id)) },
            ),
            AppContextMenuItem(
                label = openInstanceFolderLabel,
                imageVector = Lucide.Folder,
                onClick = { onEvent(InstanceActionsEvent.OnOpenInstanceFolderClick(instance.id)) },
            ),
            instance.modpackInfo?.let { info ->
                AppContextMenuItem(
                    label = openProjectLabel!!,
                    imageVector = Lucide.ArrowRight,
                    onClick = {
                        onEvent(
                            InstanceActionsEvent.OnOpenProjectFromInstanceClick(
                                instance.id,
                                info.platformId,
                                info.projectId
                            )
                        )
                    },
                )
            },
            if (instance.status is InstanceStatus.Idle || instance.status is InstanceStatus.Failed) {
                AppContextMenuItem(
                    label = copyLabel,
                    imageVector = Lucide.Copy,
                    onClick = { onEvent(InstanceActionsEvent.OnCopyInstanceClick(instance.id, instance.name)) },
                )
            } else null,
            if (instance.status is InstanceStatus.Idle || instance.status is InstanceStatus.Failed) {
                AppContextMenuItem(
                    label = exportLabel,
                    imageVector = Lucide.Share,
                    onClick = { onEvent(InstanceActionsEvent.OnExportInstanceClick(instance.id, instance.name)) },
                )
            } else null,
            AppContextMenuItem(
                label = deleteLabel,
                imageVector = Lucide.Trash,
                contentColor = errorColor,
                onClick = { onEvent(InstanceActionsEvent.OnDeleteInstanceClick(instance.id)) },
            ),
        )
    }
}

@Composable
context(_: SharedElementTransitionScope)
fun BriefInstanceCardLayout(
    modifier: Modifier,
    interactionSource: MutableInteractionSource? = null,
    onCardClick: (() -> Unit)? = null,
    forceShowAction: Boolean = false,
    contextMenuItems: () -> List<AppContextMenuItem> = { emptyList() },
    icon: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    title: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    type: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    loaderVersion: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    actionArea: @Composable () -> Unit,
) {
    val hovered: Boolean by interactionSource?.collectIsHoveredAsState() ?: remember { mutableStateOf(false) }
    AppContextMenuArea(items = contextMenuItems) {
        Column(
            modifier = modifier.appCard(
                interactionSource = interactionSource,
                onClick = onCardClick
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            icon()
            title()
            AppAnimatedContent(
                targetState = hovered || forceShowAction,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) {
                Box(
                    modifier = Modifier.height(40.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (it) {
                        actionArea()
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            type()
                            loaderVersion()
                        }
                    }
                }
            }
        }
    }
}

enum class InstanceSharedElementOrigin {
    Home, InstanceList
}

@Composable
context(scope: SharedElementTransitionScope)
fun Modifier.instanceSharedElement(
    instanceId: InstanceId,
    key: String,
    origin: InstanceSharedElementOrigin? = null
): Modifier = with(scope) {
    origin?.let {
        appSharedElement(
            sharedContentState = rememberSharedContentState("$it/instance/$instanceId/$key"),
            animatedVisibilityScope = requireAnimatedScope(SharedElementTransitionScope.AnimatedScope.Navigation)
        )
    } ?: InstanceSharedElementOrigin.entries.fold(this@instanceSharedElement) { modifier, it ->
        modifier.instanceSharedElement(instanceId, key, it)
    }
}
