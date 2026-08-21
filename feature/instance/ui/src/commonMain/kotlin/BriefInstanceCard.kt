package app.vercors.feature.instance.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.core.ui.animation.appSharedElement
import app.vercors.core.ui.component.AppIcon
import app.vercors.core.ui.component.AppLoadingPlaceholder
import app.vercors.core.ui.component.AppLoadingTextPlaceholder
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.modifier.appCard
import app.vercors.feature.instance.domain.model.InstanceId
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide
import com.slack.circuit.sharedelements.SharedElementTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BriefInstanceCard(
    instance: BriefInstanceView?,
    onInstanceClick: (InstanceId) -> Unit,
    modifier: Modifier,
    origin: InstanceSharedElementOrigin
) = SharedElementTransitionScope {
    if (instance != null) {
        val interactionSource = remember { MutableInteractionSource() }

        BriefInstanceCardLayout(
            modifier = modifier.hoverable(interactionSource = interactionSource)
                .instanceSharedElement(instance.id, "card", origin),
            interactionSource = interactionSource,
            onClick = { onInstanceClick(instance.id) },
            icon = {
                if (instance.iconFilePath != null) {
                    Image(
                        painter = rememberInstanceIconPainter(instance.iconFilePath, instance.updatedAt),
                        contentDescription = instance.name,
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp))
                            .instanceSharedElement(instance.id, "icon", origin),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AppIcon(
                        imageVector = Lucide.Gamepad2,
                        contentDescription = instance.name,
                        modifier = Modifier.size(36.dp).instanceSharedElement(instance.id, "icon", origin),
                    )
                }
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
        )
    } else {
        BriefInstanceCardLayout(
            modifier = modifier,
            icon = { AppLoadingPlaceholder(Modifier.size(36.dp)) },
            title = { AppLoadingTextPlaceholder(Modifier.width(100.dp)) },
            type = { AppLoadingTextPlaceholder(Modifier.width(50.dp)) },
            loaderVersion = { AppLoadingTextPlaceholder(Modifier.width(50.dp)) }
        )
    }
}

@Composable
context(_: SharedElementTransitionScope)
fun BriefInstanceCardLayout(
    modifier: Modifier,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null,
    icon: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    title: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    type: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    loaderVersion: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.appCard(
            interactionSource = interactionSource,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        icon()
        title()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            type()
            loaderVersion()
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
