package app.vercors.launcher.gui.instance

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
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
import app.vercors.shared.ui.component.AppIcon
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.modifier.appCard
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide

@Composable
context(
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
)
fun BriefInstanceCard(
    instance: BriefInstanceView,
    onInstanceClick: (InstanceId) -> Unit,
    modifier: Modifier
) = with(sharedTransitionScope) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .appCard(interactionSource = interactionSource) { onInstanceClick(instance.id) }
            .hoverable(interactionSource = interactionSource)
            .sharedElement(
                sharedContentState = rememberSharedContentState("instance/${instance.id}/card"),
                animatedVisibilityScope = animatedVisibilityScope
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        AppIcon(
            imageVector = Lucide.Gamepad2,
            contentDescription = instance.name,
            modifier = Modifier.size(36.dp)
                .sharedElement(
                    sharedContentState = rememberSharedContentState("instance/${instance.id}/icon"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
        )
        AppText(
            text = instance.name,
            fontWeight = FontWeight.SemiBold,
            minLines = 2,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.sharedElement(
                sharedContentState = rememberSharedContentState("instance/${instance.id}/title"),
                animatedVisibilityScope = animatedVisibilityScope
            )
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppText(
                text = instance.version,
                fontSize = 12.sp,
                singleLine = true,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState("instance/${instance.id}/version"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )
            AppText(
                text = instance.loader,
                fontSize = 12.sp,
                singleLine = true,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState("instance/${instance.id}/loader"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )
        }
    }
}
