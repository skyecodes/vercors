package app.vercors.feature.instance.ui.details

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.animation.appAnimateColorAsState
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppError
import app.vercors.core.ui.layout.AppLoading
import app.vercors.core.ui.layout.AppTabGroup
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.feature.game.ui.*
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.delete.ConfirmDeleteDialog
import app.vercors.shared.ui.*
import com.composables.icons.lucide.*
import com.composeunstyled.ProvideTextStyle
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuit.sharedelements.SharedElementTransitionScope
import dev.zacsweers.metro.AppScope
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import opensavvy.state.progressive.onFailure
import opensavvy.state.progressive.onIncomplete
import opensavvy.state.progressive.onSuccess
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@CircuitInject(InstanceDetailsScreen::class, AppScope::class)
@Composable
fun InstanceDetails(
    store: Store<InstanceDetailsState, InstanceDetailsEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    with(eventSink) {
        state.instance.onSuccess {
            InstanceDetails(it, state, modifier)
        }
        state.instance.onFailure {
            AppError(it.message)
        }
        state.instance.onIncomplete {
            AppLoading()
        }

        if (state.promptDeleteConfirmation) {
            OverlayEffect {
                val result = show(ConfirmDeleteDialog())
                eventSink(InstanceDetailsEvent.ConfirmDelete(result))
            }
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceDetails(
    instance: InstanceView,
    state: InstanceDetailsState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InstanceHeader(
            instance = instance,
            isInstanceEdited = state.isInstanceEdited
        )
        AppTabGroup(
            selectedTab = state.selectedTab,
            onSelectedTabChange = { eventSink(InstanceDetailsEvent.SelectTab(it)) },
            tabs = InstanceDetailsTab.entries.filter { it != InstanceDetailsTab.Logs || state.logs != null },
            orientation = Orientation.Vertical,
            tabTitle = { tab ->
                val backgroundColor by appAnimateColorAsState(if (selected) Theme[colors][surface2] else Theme[colors][surface])
                Row(
                    modifier = Modifier
                        .background(backgroundColor)
                        .fillMaxWidth()
                        .pointerHoverIcon(PointerIcon.Hand, true)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconTextButtonContent(
                        text = tab.title,
                        imageVector = tab.icon
                    )
                }
            }
        ) {
            when (it) {
                InstanceDetailsTab.Options -> InstanceOptionsTab(instance, state)
                InstanceDetailsTab.Mods -> InstanceModsTab(state)
                InstanceDetailsTab.ResourcePacks -> InstanceResourcePacksTab(state)
                InstanceDetailsTab.ShaderPacks -> InstanceShaderPacksTab(state)
                InstanceDetailsTab.DataPacks -> InstanceDataPacksTab(state)
                InstanceDetailsTab.Worlds -> InstanceWorldsTab(state)
                InstanceDetailsTab.Logs -> state.logs?.let { logs -> InstanceLogsTab(logs) }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceHeader(instance: InstanceView, isInstanceEdited: Boolean) = SharedElementTransitionScope {
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
                    text = instance.gameVersion,
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
            }
        }

        AppAnimatedVisibility(isInstanceEdited) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = stringResource(InstanceRes.string.changes),
                )

                AppIconTextButton(
                    onClick = { eventSink(InstanceDetailsEvent.CancelEdit) },
                    text = stringResource(SharedRes.string.cancel),
                    backgroundColor = Theme[colors][surface2],
                    contentColor = Theme[colors][onBackground],
                    imageVector = Lucide.X,
                )

                AppIconTextButton(
                    onClick = { eventSink(InstanceDetailsEvent.SaveInstance) },
                    text = stringResource(SharedRes.string.save),
                    backgroundColor = Theme[colors][success],
                    contentColor = Theme[colors][onSuccess],
                    imageVector = Lucide.Save,
                )
            }
        }

        AppAnimatedVisibility(!isInstanceEdited) {
            when (instance.status) {
                is InstanceStatus.Idle, is InstanceStatus.Failed -> AppIconTextButton(
                    onClick = { eventSink(InstanceDetailsEvent.Launch) },
                    text = stringResource(InstanceRes.string.launch),
                    backgroundColor = Theme[colors][success],
                    contentColor = Theme[colors][onSuccess],
                    imageVector = Lucide.Play,
                )

                is InstanceStatus.Launching -> AppIconTextButton(
                    onClick = { eventSink(InstanceDetailsEvent.Stop) },
                    text = stringResource(SharedRes.string.cancel),
                    backgroundColor = Theme[colors][error],
                    contentColor = Theme[colors][onError],
                    imageVector = Lucide.Square,
                )

                is InstanceStatus.Running -> AppIconTextButton(
                    onClick = { eventSink(InstanceDetailsEvent.Stop) },
                    text = stringResource(InstanceRes.string.stop),
                    backgroundColor = Theme[colors][error],
                    contentColor = Theme[colors][onError],
                    imageVector = Lucide.Square,
                )

                is InstanceStatus.Stopping -> AppIconTextButton(
                    onClick = { eventSink(InstanceDetailsEvent.Kill) },
                    text = stringResource(InstanceRes.string.kill),
                    backgroundColor = Theme[colors][error],
                    contentColor = Theme[colors][onError],
                    imageVector = Lucide.X,
                )
            }
        }

        AppMenuButton(
            imageVector = Lucide.EllipsisVertical,
            backgroundColor = Theme[colors][surface2],
            menuModifier = Modifier.offset(y = 4.dp),
            anchor = AppDropdownPanelAnchor.BottomEnd,
            menuContent = {
                AppMenuItem(
                    onClick = { eventSink(InstanceDetailsEvent.Delete) },
                    text = stringResource(SharedRes.string.delete),
                    imageVector = Lucide.Trash,
                    contentColor = Theme[colors][error]
                )
            }
        )
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceOptionsTab(instance: InstanceView, state: InstanceDetailsState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        InstanceGeneralOptionsSection(instance, state)
        InstanceJavaOptionsSection(state)
    }
}

context(eventSink: EventSink<InstanceDetailsEvent>)
private fun LazyListScope.InstanceGeneralOptionsSection(instance: InstanceView, state: InstanceDetailsState) {
    InstanceTabSection(
        titleRes = InstanceRes.string.general,
        icon = Lucide.Info
    ) {
        item(InstanceRes.string.instanceName) {
            AppTextField(
                label = stringResource(InstanceRes.string.instanceName) + if (state.isNameEdited) "*" else "",
                initialValue = state.editedInstance.name ?: instance.name,
                onValueChange = { eventSink(InstanceDetailsEvent.UpdateInstance { state -> state.copy(name = it) }) },
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.widthIn(min = 300.dp)
            )
        }
        item(GameRes.string.gameVersion) {
            val gameVersion = remember(state.gameVersionList, state.editedInstance.gameVersion, instance.gameVersion) {
                state.gameVersionList.find { it.id == (state.editedInstance.gameVersion ?: instance.gameVersion) }
            }

            GameVersionSelector(
                selectedVersion = gameVersion,
                versionList = state.gameVersionList,
                onVersionChange = {
                    it?.let {
                        eventSink(InstanceDetailsEvent.UpdateInstance { state ->
                            state.copy(
                                gameVersion = it.id
                            )
                        })
                    }
                },
                backgroundColor = Theme[colors][surface2],
                label = stringResource(GameRes.string.gameVersion) + if (state.isGameVersionEdited) "*" else ""
            )
        }

        InstanceGeneralOptionsLoader(state, instance)
    }
}

context(eventSink: EventSink<InstanceDetailsEvent>)
private fun LazyListScope.InstanceGeneralOptionsLoader(
    state: InstanceDetailsState,
    instance: InstanceView
) {
    val loaderType = when (val edit = state.editedInstance.modLoader) {
        is InstanceModLoaderEdit.Loader -> edit.type
        InstanceModLoaderEdit.Vanilla -> null
        null -> instance.loaderType
    }

    item(GameRes.string.loader) {
        LoaderSelector(
            selectedLoader = loaderType,
            availableLoaders = state.loaderList,
            onLoaderChange = { eventSink(InstanceDetailsEvent.OnLoaderChange(it)) },
            backgroundColor = Theme[colors][surface2],
            label = stringResource(GameRes.string.loader) + if (state.isModLoaderEdited) "*" else ""
        )
    }

    if (loaderType != null) {
        item(GameRes.string.loaderVersion) {
            val versionString = when (val edit = state.editedInstance.modLoader) {
                is InstanceModLoaderEdit.Loader -> edit.version
                else -> instance.loaderVersion
            }
            val selectedVersion = remember(state.loaderVersionList, versionString) {
                state.loaderVersionList.find { it.version == versionString }
            }

            LoaderVersionSelector(
                selectedVersion = selectedVersion,
                versionList = state.loaderVersionList,
                onVersionChange = { it?.let { eventSink(InstanceDetailsEvent.OnLoaderVersionChange(it.version)) } },
                backgroundColor = Theme[colors][surface2],
            )
        }
    }
}

private fun LazyListScope.InstanceJavaOptionsSection(state: InstanceDetailsState) {
    InstanceTabSection(
        titleRes = InstanceRes.string.java,
        icon = Lucide.Coffee
    ) {

    }
}

private fun LazyListScope.InstanceTabSection(
    titleRes: StringResource,
    icon: ImageVector,
    content: LazyListScope.() -> Unit
) {
    stickyHeader(titleRes) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            AppText(
                text = stringResource(titleRes),
                style = Theme[textStyles][title],
            )
        }
    }
    content()
}

private fun LazyListScope.InstanceTabRow(
    title: StringResource,
    description: StringResource? = null,
    content: @Composable RowScope.() -> Unit
) {
    item(title.key) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                AppText(text = stringResource(title), fontWeight = FontWeight.SemiBold)
                description?.let { AppText(text = stringResource(it), style = Theme[textStyles][label]) }
            }
            content()
        }
    }
}

@Composable
private fun InstanceModsTab(state: InstanceDetailsState) {
    AppText("Mods")
}

@Composable
private fun InstanceResourcePacksTab(state: InstanceDetailsState) {
    AppText("Resource Packs")
}

@Composable
private fun InstanceShaderPacksTab(state: InstanceDetailsState) {
    AppText("Shader Packs")
}

@Composable
private fun InstanceDataPacksTab(state: InstanceDetailsState) {
    AppText("Data Packs")
}

@Composable
private fun InstanceWorldsTab(state: InstanceDetailsState) {
    AppText("Worlds")
}

@Composable
private fun InstanceLogsTab(logs: List<InstanceLogEventView>) {
    val scrollState = rememberScrollState()
    SelectionContainer(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().appCard().padding(horizontal = 8.dp).verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ProvideTextStyle(Theme[textStyles][console]) {
                logs.forEachIndexed { index, it ->
                    InstanceLogMessage(it, index, logs)
                }
            }
        }

        LaunchedEffect(logs) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }
}

@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
private fun InstanceLogMessage(
    it: InstanceLogEventView,
    index: Int,
    logs: List<InstanceLogEventView>
) {
    Row(
        modifier = when (index) {
            0 -> Modifier.padding(top = 8.dp)
            logs.lastIndex -> Modifier.padding(bottom = 8.dp)
            else -> Modifier
        }.height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (it.message.isEmpty()) AppText(" ") else {
            val timezone = TimeZone.currentSystemDefault()
            val format = remember {
                LocalDateTime.Format {
                    hour()
                    char(':')
                    minute()
                    char(':')
                    second()
                }
            }
            val color = if (it.isLauncher) Theme[colors][primary] else when (it.level) {
                InstanceLogEventView.Level.TRACE, InstanceLogEventView.Level.DEBUG, InstanceLogEventView.Level.INFO -> Theme[colors][success]
                InstanceLogEventView.Level.WARN -> Theme[colors][warn]
                InstanceLogEventView.Level.ERROR, InstanceLogEventView.Level.FATAL -> Theme[colors][error]
            }
            AppText(
                text = remember(it.timestamp, timezone, format) {
                    it.timestamp.toLocalDateTime(timezone).format(format)
                },
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center,
                color = Theme[colors][onBackground].copy(alpha = 0.5f)
            )
            Box(
                modifier = Modifier.background(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ).padding(horizontal = 4.dp).width(40.dp).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = it.level.name,
                    color = color
                )
            }
            AppText(
                text = it.message,
                color = color
            )
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
        is InstanceStatus.Stopping -> stringResource(InstanceRes.string.stopping)
        is InstanceStatus.Failed -> stringResource(InstanceRes.string.failed)
    }

private val InstanceDetailsTab.icon: ImageVector
    @Stable get() = when (this) {
        InstanceDetailsTab.Logs -> Lucide.Logs
        InstanceDetailsTab.Options -> Lucide.Settings2
        InstanceDetailsTab.Mods -> Lucide.Box
        InstanceDetailsTab.ResourcePacks -> Lucide.Paintbrush
        InstanceDetailsTab.ShaderPacks -> Lucide.Glasses
        InstanceDetailsTab.DataPacks -> Lucide.Braces
        InstanceDetailsTab.Worlds -> Lucide.Earth
    }

private val InstanceDetailsTab.title: String
    @Composable get() = when (this) {
        InstanceDetailsTab.Logs -> stringResource(InstanceRes.string.logs)
        InstanceDetailsTab.Options -> stringResource(InstanceRes.string.options)
        InstanceDetailsTab.Mods -> stringResource(InstanceRes.string.mods)
        InstanceDetailsTab.ResourcePacks -> stringResource(InstanceRes.string.resourcePacks)
        InstanceDetailsTab.ShaderPacks -> stringResource(InstanceRes.string.shaderPacks)
        InstanceDetailsTab.DataPacks -> stringResource(InstanceRes.string.dataPacks)
        InstanceDetailsTab.Worlds -> stringResource(InstanceRes.string.worlds)
    }
