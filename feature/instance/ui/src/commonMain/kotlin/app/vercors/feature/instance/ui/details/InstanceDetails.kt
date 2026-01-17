package app.vercors.feature.instance.ui.details

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.ui.*
import app.vercors.shared.ui.*
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.sharedelements.SharedElementTransitionScope
import dev.zacsweers.metro.AppScope
import org.jetbrains.compose.resources.stringResource

@CircuitInject(InstanceDetailsScreen::class, AppScope::class)
@Composable
fun InstanceDetails(
    store: Store<InstanceDetailsState, InstanceDetailsEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    with(eventSink) {
        when (state.instance) {
            is Resource.Loaded -> when (val res = state.instance.result) {
                is Result.Failure -> AppError(res.error)
                is Result.Success -> InstanceDetails(res.data, state.logs, modifier)
            }

            else -> AppLoading()
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
fun InstanceDetails(
    instance: InstanceView,
    logs: List<InstanceLogEventView>?,
    modifier: Modifier = Modifier
) = SharedElementTransitionScope {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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

            if (instance.status is InstanceStatus.Launching) {
                Column(
                    modifier = Modifier.width(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                ) {
                    AppText(
                        text = instance.status.text
                    )
                    instance.status.progress?.let {
                        AppProgressIndicator(
                            progress = it,
                            backgroundColor = Theme[colors][surface2],
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            when (instance.status) {
                is InstanceStatus.Idle, is InstanceStatus.Failed -> AppIconTextButton(
                    onClick = { eventSink(InstanceDetailsEvent.Launch) },
                    text = stringResource(InstanceRes.string.launch),
                    backgroundColor = Theme[colors][success],
                    contentColor = Theme[colors][onSuccess],
                    imageVector = Lucide.Play,
                )

                is InstanceStatus.Launching -> AppIconTextButton(
                    onClick = { eventSink(InstanceDetailsEvent.Cancel) },
                    text = stringResource(SharedRes.string.cancel),
                    backgroundColor = Theme[colors][error],
                    contentColor = Theme[colors][onError],
                    imageVector = Lucide.Square,
                )

                is InstanceStatus.Running -> AppIconTextButton(
                    onClick = { eventSink(InstanceDetailsEvent.Cancel) },
                    text = stringResource(InstanceRes.string.stop),
                    backgroundColor = Theme[colors][error],
                    contentColor = Theme[colors][onError],
                    imageVector = Lucide.Square,
                )
            }

            AppMenuButton(
                imageVector = Lucide.EllipsisVertical,
                backgroundColor = Theme[colors][surface2],
                menuModifier = Modifier.offset(y = 4.dp),
                anchor = AppDropdownPanelAnchor.BottomEnd,
                menuContent = {
                    AppMenuItem(
                        text = stringResource(SharedRes.string.delete),
                        imageVector = Lucide.Trash,
                        onClick = { eventSink(InstanceDetailsEvent.Delete) },
                        contentColor = Theme[colors][error]
                    )
                }
            )
        }

        logs?.let { logs ->
            val scrollState = rememberScrollState()
            SelectionContainer(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxSize().appCard().padding(horizontal = 8.dp).verticalScroll(scrollState),
                ) {
                    logs.forEachIndexed { index, it ->
                        AppText(
                            text = it.message,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = if (it.isLauncher) Theme[colors][primary] else when (it.level) {
                                InstanceLogEventView.Level.TRACE, InstanceLogEventView.Level.DEBUG, InstanceLogEventView.Level.INFO -> Color.Unspecified
                                InstanceLogEventView.Level.WARN -> Theme[colors][warn]
                                InstanceLogEventView.Level.ERROR, InstanceLogEventView.Level.FATAL -> Theme[colors][error]
                            },
                            modifier = when (index) {
                                0 -> Modifier.padding(top = 8.dp)
                                logs.lastIndex -> Modifier.padding(bottom = 8.dp)
                                else -> Modifier
                            },
                        )
                    }
                }

                LaunchedEffect(logs) {
                    scrollState.scrollTo(scrollState.maxValue)
                }
            }
        }
    }
}

private val InstanceStatus.text: String
    @Composable get() = when (this) {
        InstanceStatus.Idle -> stringResource(InstanceRes.string.idle)
        is InstanceStatus.Launching.ValidatingToken -> stringResource(InstanceRes.string.validatingToken)
        is InstanceStatus.Launching.RefreshingToken -> stringResource(InstanceRes.string.refreshingToken)
        is InstanceStatus.Launching.LoadingMetadata -> stringResource(InstanceRes.string.loadingMetadata)
        is InstanceStatus.Launching.ValidatingFiles -> stringResource(InstanceRes.string.validatingFiles)
        is InstanceStatus.Launching.DownloadingFiles -> stringResource(InstanceRes.string.downloadingFiles)
        is InstanceStatus.Running -> stringResource(InstanceRes.string.running)
        is InstanceStatus.Failed -> stringResource(InstanceRes.string.failed)
    }
