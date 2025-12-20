package app.vercors.launcher.gui.instance

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.launcher.common.domain.instance.model.InstanceId
import app.vercors.shared.ui.animation.appSharedElement
import app.vercors.shared.ui.component.AppIcon
import app.vercors.shared.ui.component.AppLoadingPlaceholder
import app.vercors.shared.ui.component.AppLoadingTextPlaceholder
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.modifier.appCard
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide

@Composable
context(
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
)
fun BriefInstanceCard2(
    instance: BriefInstanceView?,
    onInstanceClick: (InstanceId) -> Unit,
    modifier: Modifier
) = with(sharedTransitionScope) {
    if (instance != null) {
        val interactionSource = remember { MutableInteractionSource() }

        BriefInstanceCardLayout(
            modifier = modifier
                .hoverable(interactionSource = interactionSource)
                .appSharedElement(
                    sharedContentState = rememberSharedContentState("instance/${instance.id}/card"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
            interactionSource = interactionSource,
            onClick = { onInstanceClick(instance.id) },
            icon = {
                AppIcon(
                    imageVector = Lucide.Gamepad2,
                    contentDescription = instance.name,
                    modifier = Modifier.size(36.dp)
                        .appSharedElement(
                            sharedContentState = contextOf<SharedTransitionScope>()
                                .rememberSharedContentState("instance/${instance.id}/icon"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
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
                    modifier = Modifier.appSharedElement(
                        sharedContentState = contextOf<SharedTransitionScope>()
                            .rememberSharedContentState("instance/${instance.id}/title"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
            },
            type = {
                AppText(
                    text = instance.type,
                    fontSize = 12.sp,
                    modifier = Modifier.appSharedElement(
                        sharedContentState = contextOf<SharedTransitionScope>()
                            .rememberSharedContentState("instance/${instance.id}/type"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
            },
            loaderVersion = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    AppText(
                        text = instance.loader,
                        fontSize = 12.sp,
                        modifier = Modifier.appSharedElement(
                            sharedContentState = contextOf<SharedTransitionScope>()
                                .rememberSharedContentState("instance/${instance.id}/loader"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    )
                    AppText(
                        text = instance.version,
                        fontSize = 12.sp,
                        modifier = Modifier.appSharedElement(
                            sharedContentState = contextOf<SharedTransitionScope>()
                                .rememberSharedContentState("instance/${instance.id}/version"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
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
context(
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
fun BriefInstanceCardLayout(
    modifier: Modifier,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null,
    icon: @Composable context(AnimatedContentScope, SharedTransitionScope) ColumnScope.() -> Unit,
    title: @Composable context(AnimatedContentScope, SharedTransitionScope) ColumnScope.() -> Unit,
    type: @Composable context(AnimatedContentScope, SharedTransitionScope) ColumnScope.() -> Unit,
    loaderVersion: @Composable context(AnimatedContentScope, SharedTransitionScope) ColumnScope.() -> Unit,
) {
    return Column(
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
