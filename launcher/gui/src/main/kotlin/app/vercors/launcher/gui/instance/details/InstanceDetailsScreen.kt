package app.vercors.launcher.gui.instance.details

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.shared.domain.Resource
import app.vercors.shared.domain.Result
import app.vercors.shared.ui.component.AppIcon
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.layout.AppError
import app.vercors.shared.ui.layout.AppLoading
import app.vercors.shared.ui.modifier.appCard
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
context(
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
fun InstanceDetailsScreen(
    container: InstanceDetailsContainer
) = with(container.store) {
    val state by subscribe { action ->
        when (action) {
            else -> {}
        }
    }

    InstanceDetailsScreen(state)
}

@Composable
context(
    store: IntentReceiver<InstanceDetailsIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
fun InstanceDetailsScreen(state: InstanceDetailsState) {
    when (state.instance) {
        is Resource.Loaded -> when (val res = state.instance.result) {
            is Result.Error -> AppError(res.throwable)
            is Result.Success -> InstanceDetailsScreen(state, res.data)
        }

        else -> AppLoading()
    }
}

@Composable
context(
    store: IntentReceiver<InstanceDetailsIntent>,
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
)
fun InstanceDetailsScreen(
    state: InstanceDetailsState,
    instance: InstanceView
) = with(sharedTransitionScope) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            modifier = Modifier.appCard().padding(8.dp).fillMaxWidth().sharedElement(
                sharedContentState = rememberSharedContentState("instance/${instance.id}/card"),
                animatedVisibilityScope = animatedVisibilityScope
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                imageVector = Lucide.Gamepad2,
                contentDescription = instance.name,
                modifier = Modifier.size(48.dp)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState("instance/${instance.id}/icon"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AppText(
                    text = instance.name,
                    fontWeight = FontWeight.SemiBold,
                    singleLine = true,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState("instance/${instance.id}/title"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
                }
            }
        }
    }
}
