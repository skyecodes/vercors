package app.vercors.feature.instance.ui.details

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.core.ui.CoreRes
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.animation.appAnimateColorAsState
import app.vercors.core.ui.animation.appAnimateFloatAsState
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppError
import app.vercors.core.ui.layout.AppLoading
import app.vercors.core.ui.layout.AppTabGroup
import app.vercors.core.ui.layout.AppTabScope
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.search
import app.vercors.core.ui.theme.*
import app.vercors.feature.game.ui.*
import app.vercors.feature.instance.domain.model.InstanceFolder
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.delete.ConfirmDeleteDialog
import app.vercors.feature.instance.ui.delete.ConfirmRemoveDialog
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.ui.*
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.lucide.*
import com.composeunstyled.ProvideTextStyle
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuit.sharedelements.SharedElementTransitionScope
import dev.zacsweers.metro.AppScope
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.flow.filter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import okio.Path.Companion.toPath
import opensavvy.state.progressive.onFailure
import opensavvy.state.progressive.onIncomplete
import opensavvy.state.progressive.onSuccess
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

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

        state.promptRemoveConfirmation?.let { pending ->
            OverlayEffect(pending) {
                val result = show(ConfirmRemoveDialog(pending.title))
                eventSink(InstanceDetailsEvent.OnConfirmRemove(result))
            }
        }

        if (state.promptFinishInstallation) {
            OverlayEffect {
                show(state.finishInstallationDialog)
                eventSink(InstanceDetailsEvent.OnFinishInstallationDismiss)
            }
        }

        state.promptInstallJava?.let { majorVersion ->
            OverlayEffect(majorVersion) {
                val result = show(ConfirmInstallJavaDialog(majorVersion))
                if (result) eventSink(InstanceDetailsEvent.OnInstallJavaConfirm)
                else eventSink(InstanceDetailsEvent.OnInstallJavaDismiss)
            }
        }

        if (state.promptJavaInstall) {
            OverlayEffect {
                show(state.installJavaProgressDialog)
                eventSink(InstanceDetailsEvent.OnJavaInstallDialogDismiss)
            }
        }

        if (state.promptLogin) {
            OverlayEffect {
                show(state.loginDialog)
                eventSink(InstanceDetailsEvent.CancelLogin)
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
            tabTitle = { tab -> InstanceDetailsTabTitle(tab, state) }
        ) { tab -> InstanceDetailsTabContent(tab, instance, state) }
    }
}

@Composable
private fun AppTabScope.InstanceDetailsTabTitle(tab: InstanceDetailsTab, state: InstanceDetailsState) {
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
        val installedFiles = when (tab) {
            InstanceDetailsTab.Mods -> state.installedMods
            InstanceDetailsTab.ResourcePacks -> state.installedResourcePacks
            InstanceDetailsTab.ShaderPacks -> state.installedShaderPacks
            else -> null
        }
        val title = if (installedFiles != null) {
            "${tab.title} (${installedFiles.count { it.enabled }})"
        } else {
            tab.title
        }
        AppIconTextButtonContent(
            text = title,
            imageVector = tab.icon
        )
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceDetailsTabContent(tab: InstanceDetailsTab, instance: InstanceView, state: InstanceDetailsState) {
    when (tab) {
        InstanceDetailsTab.Options -> InstanceOptionsTab(instance, state)
        InstanceDetailsTab.Mods -> InstanceModsTab(state)
        InstanceDetailsTab.ResourcePacks -> InstanceResourcePacksTab(state)
        InstanceDetailsTab.ShaderPacks -> InstanceShaderPacksTab(state)
        InstanceDetailsTab.DataPacks -> InstanceDataPacksTab(state)
        InstanceDetailsTab.Worlds -> InstanceWorldsTab(state)
        InstanceDetailsTab.Logs -> state.logs?.let { InstanceLogsTab(state) }
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
        InstanceHeaderIcon(instance)
        InstanceHeaderInfo(instance)
        InstanceHeaderStatus(instance)
        InstanceHeaderEditActions(isInstanceEdited)
        InstanceHeaderActionButton(instance, isInstanceEdited)
        InstanceHeaderMenu(instance)
    }
}

@Composable
context(_: SharedElementTransitionScope, eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceHeaderIcon(instance: InstanceView) {
    val pickerLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let { eventSink(InstanceDetailsEvent.OnIconSelected(it.path)) }
    }
    Box(
        modifier = Modifier.size(48.dp).instanceSharedElement(instance.id, "icon")
            .clip(RoundedCornerShape(8.dp))
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(onClick = pickerLauncher::launch)
    ) {
        if (instance.iconFilePath != null) {
            Image(
                painter = rememberInstanceIconPainter(instance.iconFilePath, instance.updatedAt),
                contentDescription = instance.name,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            AppIcon(
                imageVector = Lucide.Gamepad2,
                contentDescription = instance.name,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
context(_: SharedElementTransitionScope)
private fun RowScope.InstanceHeaderInfo(instance: InstanceView) {
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
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceHeaderStatus(instance: InstanceView) {
    if (instance.status !is InstanceStatus.Launching && instance.status !is InstanceStatus.Installing) return

    Column(
        modifier = Modifier.width(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        AppText(
            text = instance.status.text
        )
        instance.status.progress?.let {
            val progress by appAnimateFloatAsState(it)
            AppProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceHeaderEditActions(isInstanceEdited: Boolean) {
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
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceHeaderActionButton(instance: InstanceView, isInstanceEdited: Boolean) {
    AppAnimatedVisibility(!isInstanceEdited) {
        when (instance.status) {
            is InstanceStatus.Installing.AwaitingManualDownloads -> AppIconTextButton(
                onClick = { eventSink(InstanceDetailsEvent.OnFinishInstallationClick) },
                text = stringResource(InstanceRes.string.finishInstallation),
                backgroundColor = Theme[colors][primary],
                contentColor = Theme[colors][onPrimary],
                imageVector = Lucide.Download,
            )

            is InstanceStatus.Installing.Interrupted -> AppIconTextButton(
                onClick = { eventSink(InstanceDetailsEvent.OnResumeInstallationClick) },
                text = stringResource(InstanceRes.string.resumeInstallation),
                backgroundColor = Theme[colors][primary],
                contentColor = Theme[colors][onPrimary],
                imageVector = Lucide.RefreshCw,
            )

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

            is InstanceStatus.Installing -> AppIconTextButton(
                onClick = {},
                text = stringResource(InstanceRes.string.launch),
                backgroundColor = Theme[colors][success],
                contentColor = Theme[colors][onSuccess],
                imageVector = Lucide.Play,
                enabled = false,
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
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceHeaderMenu(instance: InstanceView) {
    AppMenuButton {
        if (instance.status is InstanceStatus.Installing) {
            AppMenuItem(
                onClick = { eventSink(InstanceDetailsEvent.CancelSetup) },
                text = stringResource(InstanceRes.string.cancelSetup),
                imageVector = Lucide.X,
                contentColor = Theme[colors][error]
            )
        }
        AppMenuItem(
            onClick = { eventSink(InstanceDetailsEvent.OnOpenInstanceFolderClick(InstanceFolder.Root)) },
            text = stringResource(InstanceRes.string.openInstanceFolder),
            imageVector = Lucide.FolderOpen
        )
        instance.modpackInfo?.let { modpackInfo ->
            AppMenuItem(
                onClick = {
                    eventSink(
                        InstanceDetailsEvent.OnOpenProjectClick(modpackInfo.platformId, modpackInfo.projectId)
                    )
                },
                text = stringResource(
                    InstanceRes.string.openProject,
                    modpackInfo.platformId.toPlatformDisplayName()
                ),
                imageVector = Lucide.ArrowRight
            )
        }
        if (instance.iconFilePath != null) {
            AppMenuItem(
                onClick = { eventSink(InstanceDetailsEvent.OnRemoveIconClick) },
                text = stringResource(InstanceRes.string.removeIcon),
                imageVector = Lucide.ImageOff
            )
        } else if (instance.modpackInfo != null) {
            AppMenuItem(
                onClick = { eventSink(InstanceDetailsEvent.OnRestoreIconClick) },
                text = stringResource(InstanceRes.string.restoreIcon),
                imageVector = Lucide.Image
            )
        }
        AppMenuItem(
            onClick = { eventSink(InstanceDetailsEvent.Delete) },
            text = stringResource(SharedRes.string.delete),
            imageVector = Lucide.Trash,
            contentColor = Theme[colors][error]
        )
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceOptionsTab(instance: InstanceView, state: InstanceDetailsState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .scrollable(state = rememberScrollState(), orientation = Orientation.Horizontal),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            item {
                InstanceGeneralOptionsSection(instance, state)
            }
            item {
                InstanceJavaOptionsSection(instance, state)
            }
            if (state.showNativeLibraryOptions) {
                item {
                    InstanceNativeLibraryOptionsSection(instance, state)
                }
            }
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceGeneralOptionsSection(instance: InstanceView, state: InstanceDetailsState) {
    InstanceTabSection(
        titleRes = InstanceRes.string.general,
        icon = Lucide.Info
    ) {
        AppTextField(
            label = stringResource(InstanceRes.string.instanceName) + if (state.isNameEdited) "*" else "",
            initialValue = state.editedInstance.name ?: instance.name,
            onValueChange = { eventSink(InstanceDetailsEvent.UpdateInstance { state -> state.copy(name = it) }) },
            modifier = Modifier.widthIn(min = 300.dp)
        )

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
            label = stringResource(GameRes.string.gameVersion) + if (state.isGameVersionEdited) "*" else ""
        )

        InstanceGeneralOptionsLoader(state, instance)
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceGeneralOptionsLoader(
    state: InstanceDetailsState,
    instance: InstanceView
) {
    val loaderType = when (val edit = state.editedInstance.modLoaderType) {
        is ModLoaderTypeEdit.Loader -> edit.type
        ModLoaderTypeEdit.Vanilla -> null
        null -> instance.loaderType
    }

    LoaderSelector(
        selectedLoader = loaderType,
        availableLoaders = state.loaderList,
        onLoaderChange = { eventSink(InstanceDetailsEvent.OnLoaderChange(it)) },
        label = stringResource(GameRes.string.loader) + if (state.isLoaderTypeEdited) "*" else ""
    )


    if (loaderType != null) {
        val versionString = state.editedInstance.modLoaderVersion ?: instance.loaderVersion
        val selectedVersion = remember(state.loaderVersionList, versionString) {
            state.loaderVersionList.find { it.version == versionString }
        }

        LoaderVersionSelector(
            selectedVersion = selectedVersion,
            versionList = state.loaderVersionList,
            onVersionChange = { it?.let { eventSink(InstanceDetailsEvent.OnLoaderVersionChange(it.version)) } },
            label = stringResource(GameRes.string.loaderVersion) + if (state.isLoaderVersionEdited) "*" else ""
        )
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceJavaOptionsSection(instance: InstanceView, state: InstanceDetailsState) {
    InstanceTabSection(
        titleRes = InstanceRes.string.java,
        icon = Lucide.Coffee
    ) {
        val selectedId = when (val edit = state.editedInstance.javaOverride) {
            is JavaOverrideEdit.Specific -> edit.installationId
            JavaOverrideEdit.UseDefault -> null
            null -> instance.javaInstallationId
        }
        val selected = state.availableJavaInstallations.find { it.id == selectedId }
        val defaultLabel = stringResource(InstanceRes.string.useDefaultJava)

        AppSelectField(
            value = selected,
            items = listOf(null) + state.availableJavaInstallations,
            onValueChange = { eventSink(InstanceDetailsEvent.OnJavaOverrideChange(it?.id)) },
            label = stringResource(InstanceRes.string.javaOverride) + if (state.isJavaOverrideEdited) "*" else "",
            itemRenderer = { value, _ ->
                AppSelectItem(value?.path ?: defaultLabel)
            }
        )

        val effectiveCustomOptions: JvmMemoryOptions? = when (val edit = state.editedInstance.jvmMemoryOptionsEdit) {
            is JvmMemoryOptionsEdit.Custom -> edit.options
            JvmMemoryOptionsEdit.UseDefault -> null
            null -> instance.jvmMemoryOptions
        }
        val currentOptions = effectiveCustomOptions ?: JvmMemoryOptions()
        val defaultMax = defaultMaxMemoryMb(state.totalMemoryMb)

        fun updateOptions(update: (JvmMemoryOptions) -> JvmMemoryOptions) {
            eventSink(InstanceDetailsEvent.UpdateInstance { s ->
                s.copy(jvmMemoryOptionsEdit = JvmMemoryOptionsEdit.Custom(update(currentOptions)))
            })
        }

        InstanceMemorySliderRow(
            title = InstanceRes.string.maxMemory,
            description = InstanceRes.string.maxMemoryDesc,
            value = currentOptions.maxMemoryMb,
            defaultValue = defaultMax,
            valueRange = 512..state.totalMemoryMb,
            stepSize = 128,
            onCheckedChange = { checked -> updateOptions { it.copy(maxMemoryMb = if (checked) defaultMax else null) } },
            onValueChange = { newValue ->
                updateOptions { opts ->
                    val clampedMin = opts.minMemoryMb?.let { min -> minOf(min, newValue) }
                    opts.copy(maxMemoryMb = newValue, minMemoryMb = clampedMin)
                }
            },
        )
        InstanceMemorySliderRow(
            title = InstanceRes.string.minMemory,
            description = InstanceRes.string.minMemoryDesc,
            value = currentOptions.minMemoryMb,
            defaultValue = defaultMinMemoryMb(),
            valueRange = 256..(currentOptions.maxMemoryMb ?: defaultMax),
            stepSize = 32,
            onCheckedChange = { checked -> updateOptions { it.copy(minMemoryMb = if (checked) defaultMinMemoryMb() else null) } },
            onValueChange = { newValue -> updateOptions { it.copy(minMemoryMb = newValue) } },
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(text = stringResource(InstanceRes.string.extraJvmArgs), fontWeight = FontWeight.SemiBold)
            AppText(text = stringResource(InstanceRes.string.extraJvmArgsDesc), style = Theme[textStyles][label])
            AppTextField(
                initialValue = currentOptions.extraArgs,
                onValueChange = { newValue -> updateOptions { it.copy(extraArgs = newValue) } },
                debounceMillis = 500,
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceNativeLibraryOptionsSection(instance: InstanceView, state: InstanceDetailsState) {
    InstanceTabSection(
        titleRes = InstanceRes.string.nativeLibraries,
        icon = Lucide.Package
    ) {
        val effectiveOptions: NativeLibraryOptions? = when (val edit = state.editedInstance.nativeLibraryOptionsEdit) {
            is NativeLibraryOptionsEdit.Custom -> edit.options
            NativeLibraryOptionsEdit.UseDefault -> null
            null -> instance.nativeLibraryOptions
        }
        val currentOptions = effectiveOptions ?: NativeLibraryOptions()

        fun updateOptions(update: (NativeLibraryOptions) -> NativeLibraryOptions) {
            eventSink(InstanceDetailsEvent.UpdateInstance { s ->
                s.copy(nativeLibraryOptionsEdit = NativeLibraryOptionsEdit.Custom(update(currentOptions)))
            })
        }

        InstanceLibraryOverrideRow(
            title = InstanceRes.string.useSystemGlfw,
            description = InstanceRes.string.useSystemGlfwDesc,
            checked = currentOptions.useSystemGlfw,
            path = currentOptions.glfwPath.orEmpty(),
            detectedPath = state.detectedGlfwPath,
            onCheckedChange = { checked -> updateOptions { it.copy(useSystemGlfw = checked) } },
            onPathChange = { newValue -> updateOptions { it.copy(glfwPath = newValue) } },
        )
        InstanceLibraryOverrideRow(
            title = InstanceRes.string.useSystemOpenAl,
            description = InstanceRes.string.useSystemOpenAlDesc,
            checked = currentOptions.useSystemOpenAl,
            path = currentOptions.openAlPath.orEmpty(),
            detectedPath = state.detectedOpenAlPath,
            onCheckedChange = { checked -> updateOptions { it.copy(useSystemOpenAl = checked) } },
            onPathChange = { newValue -> updateOptions { it.copy(openAlPath = newValue) } },
        )
    }
}

@Composable
private fun InstanceLibraryOverrideRow(
    title: StringResource,
    description: StringResource,
    checked: Boolean,
    path: String,
    detectedPath: String?,
    onCheckedChange: (Boolean) -> Unit,
    onPathChange: (String) -> Unit,
) {
    val pickerLauncher = rememberFilePickerLauncher(type = FileKitType.File("so")) { file ->
        file?.let { onPathChange(it.path) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        AppText(text = stringResource(title), fontWeight = FontWeight.SemiBold)
        AppText(text = stringResource(description), style = Theme[textStyles][label])
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCheckbox(checked = checked, onCheckedChange = onCheckedChange)
            AppTextField(
                initialValue = path,
                onValueChange = onPathChange,
                debounceMillis = 500,
                editable = checked,
                placeholder = { AppText(text = detectedPath ?: stringResource(InstanceRes.string.useDefaultPath)) },
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.weight(1f)
            )
            AppIconTextButton(
                onClick = pickerLauncher::launch,
                text = stringResource(InstanceRes.string.browse),
                imageVector = Lucide.FolderOpen,
                enabled = checked,
            )
        }
    }
}

@Composable
private fun InstanceMemorySliderRow(
    title: StringResource,
    description: StringResource,
    value: Int?,
    defaultValue: Int,
    valueRange: IntRange,
    stepSize: Int,
    onCheckedChange: (Boolean) -> Unit,
    onValueChange: (Int) -> Unit,
) {
    val checked = value != null
    val displayValue = (value ?: defaultValue).coerceIn(valueRange)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        AppText(text = stringResource(title), fontWeight = FontWeight.SemiBold)
        AppText(text = stringResource(description), style = Theme[textStyles][label])
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCheckbox(checked = checked, onCheckedChange = onCheckedChange)
            AppSlider(
                value = displayValue,
                onValueChange = onValueChange,
                valueRange = valueRange,
                stepSize = stepSize,
                enabled = checked,
                modifier = Modifier.weight(1f)
            )
            AppNumberField(
                value = displayValue,
                onValueChange = onValueChange,
                valueRange = valueRange,
                enabled = checked,
            )
            AppText(text = stringResource(InstanceRes.string.megabytes), style = Theme[textStyles][label])
        }
    }
}

private fun defaultMaxMemoryMb(totalMemoryMb: Int): Int = (((totalMemoryMb / 4).coerceIn(1024, 4096)) / 128) * 128
private fun defaultMinMemoryMb(): Int = 512

@Composable
private fun InstanceTabSection(
    titleRes: StringResource,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                imageVector = icon,
                contentDescription = null,
            )
            AppText(
                text = stringResource(titleRes),
                style = Theme[textStyles][title],
            )
        }
        Column(
            modifier = Modifier.appCard().padding(8.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

/** Platform names aren't translated, so this is a plain lookup rather than a string resource. */
private fun String.toPlatformDisplayName(): String = when (this) {
    "modrinth" -> "Modrinth"
    "curseforge" -> "CurseForge"
    else -> this
}

private enum class ContentSortBy {
    Name,
    LastModified
}

private val ContentSortBy.displayName: String
    @Composable get() = stringResource(
        when (this) {
            ContentSortBy.Name -> InstanceRes.string.sortByName
            ContentSortBy.LastModified -> InstanceRes.string.sortByLastModified
        }
    )

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstalledContentFileList(
    files: List<InstalledContentFileView>?,
    emptyMessage: StringResource,
    addLabel: StringResource,
    addEnabled: Boolean,
    removalKind: RemovalKind,
    importLabel: StringResource,
    importExtension: String,
    openFolderLabel: StringResource,
    contentFolder: InstanceFolder,
    onAddClick: () -> Unit,
    onEnabledChange: (fileName: String, enabled: Boolean) -> InstanceDetailsEvent
) {
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyEnabled by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf(ContentSortBy.Name) }
    val listState = rememberLazyListState()

    LaunchedEffect(searchQuery, showOnlyEnabled, sortBy) {
        listState.scrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize().appCard()) {
        InstalledContentFileListHeader(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            showOnlyEnabled = showOnlyEnabled,
            onShowOnlyEnabledChange = { showOnlyEnabled = it },
            sortBy = sortBy,
            onSortByChange = { sortBy = it },
            addLabel = addLabel,
            addEnabled = addEnabled,
            removalKind = removalKind,
            importLabel = importLabel,
            importExtension = importExtension,
            openFolderLabel = openFolderLabel,
            contentFolder = contentFolder,
            onAddClick = onAddClick
        )

        val filteredFiles = files?.filteredAndSorted(searchQuery, showOnlyEnabled, sortBy)
        InstalledContentFileListBody(
            files = files,
            filteredFiles = filteredFiles,
            emptyMessage = emptyMessage,
            removalKind = removalKind,
            listState = listState,
            onEnabledChange = onEnabledChange
        )
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstalledContentFileListHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showOnlyEnabled: Boolean,
    onShowOnlyEnabledChange: (Boolean) -> Unit,
    sortBy: ContentSortBy,
    onSortByChange: (ContentSortBy) -> Unit,
    addLabel: StringResource,
    addEnabled: Boolean,
    removalKind: RemovalKind,
    importLabel: StringResource,
    importExtension: String,
    openFolderLabel: StringResource,
    contentFolder: InstanceFolder,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTextField(
            initialValue = searchQuery,
            placeholder = { AppText(stringResource(CoreRes.string.search)) },
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f)
        )
        AppCheckbox(
            label = stringResource(InstanceRes.string.showOnlyEnabled),
            checked = showOnlyEnabled,
            onCheckedChange = onShowOnlyEnabledChange
        )
        AppSelectField(
            value = sortBy,
            items = ContentSortBy.entries,
            onValueChange = onSortByChange,
            itemRenderer = { value, isButton ->
                AppSelectItem(
                    text = if (isButton) "${stringResource(SharedRes.string.sortBy)}: ${value.displayName}"
                    else value.displayName
                )
            },
            modifier = Modifier.width(200.dp)
        )
        InstalledContentFileListMenu(
            addLabel = addLabel,
            addEnabled = addEnabled,
            removalKind = removalKind,
            importLabel = importLabel,
            importExtension = importExtension,
            openFolderLabel = openFolderLabel,
            contentFolder = contentFolder,
            onAddClick = onAddClick
        )
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstalledContentFileListMenu(
    addLabel: StringResource,
    addEnabled: Boolean,
    removalKind: RemovalKind,
    importLabel: StringResource,
    importExtension: String,
    openFolderLabel: StringResource,
    contentFolder: InstanceFolder,
    onAddClick: () -> Unit
) {
    val importPickerLauncher = rememberFilePickerLauncher(type = FileKitType.File(importExtension)) { file ->
        file?.let { eventSink(InstanceDetailsEvent.OnImportContentClick(removalKind, it.path)) }
    }
    AppMenuButton {
        AppMenuItem(
            onClick = onAddClick,
            text = stringResource(addLabel),
            imageVector = Lucide.Plus,
            enabled = addEnabled
        )
        AppMenuItem(
            onClick = importPickerLauncher::launch,
            text = stringResource(importLabel),
            imageVector = Lucide.FileUp,
            enabled = addEnabled
        )
        AppMenuItem(
            onClick = { eventSink(InstanceDetailsEvent.OnOpenInstanceFolderClick(contentFolder)) },
            text = stringResource(openFolderLabel),
            imageVector = Lucide.FolderOpen
        )
        if (contentFolder == InstanceFolder.Mods) {
            AppMenuItem(
                onClick = { eventSink(InstanceDetailsEvent.OnOpenInstanceFolderClick(InstanceFolder.Config)) },
                text = stringResource(InstanceRes.string.openConfigFolder),
                imageVector = Lucide.FolderCog
            )
        }
    }
}

private fun List<InstalledContentFileView>.filteredAndSorted(
    searchQuery: String,
    showOnlyEnabled: Boolean,
    sortBy: ContentSortBy
): List<InstalledContentFileView> = filter {
    (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)) &&
            (!showOnlyEnabled || it.enabled)
}.let { list ->
    when (sortBy) {
        ContentSortBy.Name -> list.sortedBy { it.title.lowercase() }
        ContentSortBy.LastModified -> list.sortedByDescending { it.lastModified ?: Instant.DISTANT_PAST }
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstalledContentFileListBody(
    files: List<InstalledContentFileView>?,
    filteredFiles: List<InstalledContentFileView>?,
    emptyMessage: StringResource,
    removalKind: RemovalKind,
    listState: LazyListState,
    onEnabledChange: (fileName: String, enabled: Boolean) -> InstanceDetailsEvent
) {
    when {
        files == null -> AppLoading()
        files.isEmpty() -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AppText(stringResource(emptyMessage))
        }

        filteredFiles.isNullOrEmpty() -> AppText(stringResource(InstanceRes.string.noMatchingFiles))
        else -> Row(modifier = Modifier.fillMaxSize().padding(end = 4.dp)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredFiles, key = { it.fileName }) { file ->
                    InstalledContentFileRow(file, removalKind, onEnabledChange)
                }
            }
            VerticalScrollbar(adapter = rememberScrollbarAdapter(listState))
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstalledContentFileRow(
    file: InstalledContentFileView,
    removalKind: RemovalKind,
    onEnabledChange: (fileName: String, enabled: Boolean) -> InstanceDetailsEvent
) {
    val contentAlpha = if (file.enabled) 1f else 0.4f

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (file.iconFilePath != null) {
            Image(
                painter = rememberAsyncImagePainter(model = file.iconFilePath.toPath()),
                contentDescription = file.title,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).alpha(contentAlpha),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).alpha(contentAlpha)
                    .background(Theme[colors][surface2]),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(imageVector = Lucide.Box, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }

        Column(
            modifier = Modifier.weight(1f).alpha(contentAlpha),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = file.title,
                    fontWeight = FontWeight.SemiBold,
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis
                )
                file.version?.let {
                    AppText(text = it, fontSize = 12.sp, color = Theme[colors][onBackground].copy(alpha = 0.6f))
                }
            }
            file.description?.let {
                AppText(text = it, fontSize = 12.sp, singleLine = true, overflow = TextOverflow.Ellipsis)
            }
        }

        AppMenuButton {
            AppMenuItem(
                onClick = { eventSink(onEnabledChange(file.fileName, !file.enabled)) },
                text = stringResource(if (file.enabled) InstanceRes.string.disable else InstanceRes.string.enable),
                imageVector = if (file.enabled) Lucide.EyeOff else Lucide.Eye
            )
            AppMenuItem(
                onClick = {
                    eventSink(InstanceDetailsEvent.OnRemoveClick(removalKind, file.fileName, file.title))
                },
                text = stringResource(InstanceRes.string.remove),
                imageVector = Lucide.Trash,
                contentColor = Theme[colors][error]
            )
            file.providers.forEach { provider ->
                AppMenuItem(
                    onClick = {
                        eventSink(InstanceDetailsEvent.OnOpenProjectClick(provider.platformId, provider.projectId))
                    },
                    text = stringResource(
                        InstanceRes.string.openProject,
                        provider.platformId.toPlatformDisplayName()
                    ),
                    imageVector = Lucide.ArrowRight
                )
            }
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceModsTab(state: InstanceDetailsState) {
    InstalledContentFileList(
        files = state.installedMods,
        emptyMessage = InstanceRes.string.noModsInstalled,
        addLabel = InstanceRes.string.addMod,
        addEnabled = state.instance.valueOrNull?.loaderType != null,
        removalKind = RemovalKind.Mod,
        importLabel = InstanceRes.string.importMod,
        importExtension = "jar",
        openFolderLabel = InstanceRes.string.openModsFolder,
        contentFolder = InstanceFolder.Mods,
        onAddClick = { eventSink(InstanceDetailsEvent.OnAddContentClick(ProjectType.Mod)) }
    ) { fileName, enabled ->
        InstanceDetailsEvent.OnModEnabledChange(fileName, enabled)
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceResourcePacksTab(state: InstanceDetailsState) {
    InstalledContentFileList(
        files = state.installedResourcePacks,
        emptyMessage = InstanceRes.string.noResourcePacksInstalled,
        addLabel = InstanceRes.string.addResourcePack,
        addEnabled = true,
        removalKind = RemovalKind.ResourcePack,
        importLabel = InstanceRes.string.importResourcePack,
        importExtension = "zip",
        openFolderLabel = InstanceRes.string.openResourcePacksFolder,
        contentFolder = InstanceFolder.ResourcePacks,
        onAddClick = { eventSink(InstanceDetailsEvent.OnAddContentClick(ProjectType.ResourcePack)) }
    ) { fileName, enabled ->
        InstanceDetailsEvent.OnResourcePackEnabledChange(fileName, enabled)
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceShaderPacksTab(state: InstanceDetailsState) {
    InstalledContentFileList(
        files = state.installedShaderPacks,
        emptyMessage = InstanceRes.string.noShaderPacksInstalled,
        addLabel = InstanceRes.string.addShaderPack,
        addEnabled = true,
        removalKind = RemovalKind.ShaderPack,
        importLabel = InstanceRes.string.importShaderPack,
        importExtension = "zip",
        openFolderLabel = InstanceRes.string.openShaderPacksFolder,
        contentFolder = InstanceFolder.ShaderPacks,
        onAddClick = { eventSink(InstanceDetailsEvent.OnAddContentClick(ProjectType.ShaderPack)) }
    ) { fileName, enabled ->
        InstanceDetailsEvent.OnShaderPackEnabledChange(fileName, enabled)
    }
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
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceLogsTab(state: InstanceDetailsState) {
    val logs = state.filteredLogs ?: emptyList()
    val listState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()
    var stickToBottom by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { inProgress -> !inProgress }
            .collect { stickToBottom = !listState.canScrollForward }
    }
    LaunchedEffect(logs) {
        if (stickToBottom && logs.isNotEmpty()) listState.scrollToItem(logs.lastIndex)
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InstanceLogsFilterBar(state)

        SelectionContainer(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.appCard()) {
                Row(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            .let { m -> if (state.wrapLogLines) m else m.horizontalScroll(horizontalScrollState) },
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(logs) { logEvent ->
                            ProvideTextStyle(Theme[textStyles][console]) {
                                InstanceLogMessage(logEvent, state.wrapLogLines)
                            }
                        }
                    }
                    VerticalScrollbar(adapter = rememberScrollbarAdapter(listState))
                }
                if (!state.wrapLogLines) {
                    HorizontalScrollbar(
                        adapter = rememberScrollbarAdapter(horizontalScrollState),
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceLogsFilterBar(state: InstanceDetailsState) {
    Row(
        modifier = Modifier.appCard().padding(8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppMultiSelectField(
            values = state.logLevelFilter,
            items = InstanceLogEventView.Level.entries,
            onValueChange = { eventSink(InstanceDetailsEvent.OnLogLevelFilterToggle(it)) },
            itemRenderer = { value, _ -> AppSelectItem(text = value.name) },
        )
        AppTextField(
            initialValue = state.logSearchQuery,
            onValueChange = { eventSink(InstanceDetailsEvent.OnLogSearchQueryChange(it)) },
            placeholder = { AppText(stringResource(CoreRes.string.search)) },
            modifier = Modifier.weight(1f)
        )
        AppCheckbox(
            label = stringResource(InstanceRes.string.wrapLines),
            checked = state.wrapLogLines,
            onCheckedChange = { eventSink(InstanceDetailsEvent.OnWrapLogLinesToggle) }
        )
    }
}

@Composable
private fun InstanceLogMessage(
    it: InstanceLogEventView,
    wrapLines: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
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
                color = color,
                maxLines = if (wrapLines) Int.MAX_VALUE else 1,
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
        is InstanceStatus.Launching.InstallingLoader ->
            stringResource(InstanceRes.string.installingLoader, loaderType.displayName)
        is InstanceStatus.Launching.ValidatingFiles -> stringResource(InstanceRes.string.validatingFiles)
        is InstanceStatus.Launching.DownloadingFiles -> stringResource(InstanceRes.string.downloadingGameFiles)
        is InstanceStatus.Installing.ResolvingFiles -> stringResource(InstanceRes.string.resolvingFiles)
        is InstanceStatus.Installing.DownloadingFiles -> stringResource(InstanceRes.string.downloadingFiles)
        is InstanceStatus.Installing.AwaitingManualDownloads -> stringResource(InstanceRes.string.awaitingManualDownloads)
        is InstanceStatus.Installing.Interrupted -> stringResource(InstanceRes.string.installationInterrupted)
        is InstanceStatus.Running -> stringResource(InstanceRes.string.running)
        is InstanceStatus.Stopping -> stringResource(InstanceRes.string.stopping)
        is InstanceStatus.Failed -> stringResource(InstanceRes.string.failed)
    }

private val InstanceStatus.progress: Float?
    get() = when (this) {
        is InstanceStatus.Launching.ValidatingFiles -> progress
        is InstanceStatus.Launching.DownloadingFiles -> progress
        is InstanceStatus.Installing.ResolvingFiles -> progress
        is InstanceStatus.Installing.DownloadingFiles -> progress
        else -> null
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
