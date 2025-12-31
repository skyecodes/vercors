package app.vercors.feature.instance.ui.details

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.core.domain.Resource
import app.vercors.core.domain.Result
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppError
import app.vercors.core.ui.layout.AppLoading
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.instanceSharedElement
import app.vercors.feature.instance.ui.launch
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.delete
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource
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
    val state by subscribe()

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
            is Result.Failure -> AppError(res.error)
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
            modifier = Modifier.appCard().padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
                .instanceSharedElement(instance.id, "card"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                imageVector = Lucide.Gamepad2,
                contentDescription = instance.name,
                modifier = Modifier.size(48.dp).instanceSharedElement(instance.id, "icon")
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AppText(
                    text = instance.name,
                    fontWeight = FontWeight.SemiBold,
                    singleLine = true,
                    modifier = Modifier.instanceSharedElement(instance.id, "title")
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    AppText(
                        text = "${instance.type} -",
                        fontSize = 12.sp,
                        modifier = Modifier.instanceSharedElement(instance.id, "type")
                    )
                    AppText(
                        text = instance.loader,
                        fontSize = 12.sp,
                        modifier = Modifier.instanceSharedElement(instance.id, "loader")
                    )
                    AppText(
                        text = instance.version,
                        fontSize = 12.sp,
                        modifier = Modifier.instanceSharedElement(instance.id, "version")
                    )
                }
            }

            AppIconTextButton(
                onClick = { store.intent(InstanceDetailsIntent.LaunchInstance) },
                text = stringResource(InstanceRes.string.launch),
                backgroundColor = Theme[colors][success],
                contentColor = Theme[colors][onSuccess],
                imageVector = Lucide.Play,
            )

            AppMenuButton(
                imageVector = Lucide.EllipsisVertical,
                backgroundColor = Theme[colors][surface2],
                menuModifier = Modifier.offset(y = 4.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                anchor = AppDropdownPanelAnchor.BottomEnd,
                menuContent = {
                    AppMenuItem(
                        text = stringResource(SharedRes.string.delete),
                        imageVector = Lucide.Trash,
                        onClick = { store.intent(InstanceDetailsIntent.DeleteInstance) },
                        contentColor = Theme[colors][error]
                    )
                }
            )
        }
    }
}
