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
import androidx.compose.ui.unit.sp
import app.vercors.core.ui.animation.AppAnimatedContent
import app.vercors.core.ui.animation.appSharedElement
import app.vercors.core.ui.component.AppLoadingPlaceholder
import app.vercors.core.ui.component.AppLoadingTextPlaceholder
import app.vercors.core.ui.component.AppProgressIndicator
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.modifier.appCard
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import com.slack.circuit.sharedelements.SharedElementTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BriefInstanceCard(
    instance: BriefInstanceView?,
    onInstanceClick: (InstanceId) -> Unit,
    onInstanceLaunchClick: (InstanceId) -> Unit,
    onInstanceStopClick: (InstanceId) -> Unit,
    onInstanceKillClick: (InstanceId) -> Unit,
    modifier: Modifier,
    origin: InstanceSharedElementOrigin
) = SharedElementTransitionScope {
    if (instance != null) {
        val interactionSource = remember { MutableInteractionSource() }

        BriefInstanceCardLayout(
            modifier = modifier.hoverable(interactionSource = interactionSource)
                .instanceSharedElement(instance.id, "card", origin),
            interactionSource = interactionSource,
            onCardClick = { onInstanceClick(instance.id) },
            forceShowAction = instance.status !is InstanceStatus.Idle && instance.status !is InstanceStatus.Failed,
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
                AppText(
                    text = instance.type,
                    fontSize = 12.sp,
                    modifier = Modifier.instanceSharedElement(instance.id, "type", origin),
                )
            },
            loaderVersion = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    AppText(
                        text = instance.loader,
                        fontSize = 12.sp,
                        modifier = Modifier.instanceSharedElement(instance.id, "loader", origin),
                    )
                    AppText(
                        text = instance.version,
                        fontSize = 12.sp,
                        modifier = Modifier.instanceSharedElement(instance.id, "version", origin),
                    )
                }
            },
            actionArea = {
                when (instance.status) {
                    is InstanceStatus.Idle, is InstanceStatus.Failed ->
                        InstanceLaunchButton(onClick = { onInstanceLaunchClick(instance.id) })

                    is InstanceStatus.Launching, is InstanceStatus.Installing -> AppProgressIndicator(
                        progress = instance.status.progress ?: 0f,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )

                    is InstanceStatus.Running -> InstanceStopButton(onClick = { onInstanceStopClick(instance.id) })
                    is InstanceStatus.Stopping -> InstanceKillButton(onClick = { onInstanceKillClick(instance.id) })
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
context(_: SharedElementTransitionScope)
fun BriefInstanceCardLayout(
    modifier: Modifier,
    interactionSource: MutableInteractionSource? = null,
    onCardClick: (() -> Unit)? = null,
    forceShowAction: Boolean = false,
    icon: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    title: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    type: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    loaderVersion: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    actionArea: @Composable () -> Unit,
) {
    val hovered: Boolean by interactionSource?.collectIsHoveredAsState() ?: remember { mutableStateOf(false) }
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
