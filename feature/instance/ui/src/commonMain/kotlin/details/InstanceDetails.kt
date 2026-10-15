package app.vercors.feature.instance.ui.details

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.*
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.feature.game.ui.icon
import app.vercors.feature.instance.domain.model.InstanceFolder
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.details.content.*
import app.vercors.feature.instance.ui.details.header.InstanceHeaderEvent
import app.vercors.feature.instance.ui.details.header.InstanceHeaderOverlays
import app.vercors.feature.instance.ui.details.installation.InstanceInstallationEvent
import app.vercors.feature.instance.ui.details.installation.InstanceInstallationOverlays
import app.vercors.feature.instance.ui.details.launch.InstanceLaunchEvent
import app.vercors.feature.instance.ui.details.launch.InstanceLaunchOverlays
import app.vercors.feature.instance.ui.details.logs.InstanceLogs
import app.vercors.feature.instance.ui.details.options.InstanceAdvancedOptions
import app.vercors.feature.instance.ui.details.options.InstanceJavaOptions
import app.vercors.feature.instance.ui.details.options.InstanceOptions
import app.vercors.feature.instance.ui.details.options.InstanceOptionsEvent
import app.vercors.feature.instance.ui.details.servers.InstanceServers
import app.vercors.feature.instance.ui.details.update.InstanceUpdateEvent
import app.vercors.feature.instance.ui.details.update.InstanceUpdateOverlays
import app.vercors.feature.instance.ui.details.update.InstanceUpdateState
import app.vercors.feature.instance.ui.details.worlds.InstalledWorlds
import app.vercors.feature.instance.ui.details.worlds.InstalledWorldsEvent
import app.vercors.feature.launcher.domain.model.QuickPlayTarget
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.shared.ui.*
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuit.sharedelements.SharedElementTransitionScope
import dev.zacsweers.metro.AppScope
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import nl.jacobras.humanreadable.HumanReadable
import opensavvy.state.progressive.onFailure
import opensavvy.state.progressive.onIncomplete
import opensavvy.state.progressive.onSuccess
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

        InstanceLaunchOverlays(state.launch)
        InstanceHeaderOverlays(state.header)
        InstanceInstallationOverlays(state.installation)
        InstanceUpdateOverlays(state.update)

        InstalledContentRemoveConfirmEffect(state.modsContent)
        InstalledContentRemoveConfirmEffect(state.resourcePacksContent)
        InstalledContentRemoveConfirmEffect(state.shaderPacksContent)
        state.worldsContent.state.promptRemoveConfirmation?.let { pending ->
            OverlayEffect(pending) {
                val result = show(removeContentConfirmDialog(pending.folderName.stripMinecraftFormattingForDisplay()))
                state.worldsContent.eventSink(InstalledWorldsEvent.OnConfirmRemove(result))
            }
        }
    }
}

@Composable
private fun InstalledContentRemoveConfirmEffect(content: Store<InstalledContentState, InstalledContentEvent>) {
    content.state.promptRemoveConfirmation?.let { pending ->
        OverlayEffect(pending) {
            content.eventSink(InstalledContentEvent.OnConfirmRemove(show(removeContentConfirmDialog(pending.title))))
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
        modifier = modifier.padding(top = 8.dp)
    ) {
        InstanceHeader(
            instance = instance,
            state = state,
            isInstanceEdited = state.optionsContent.state.isInstanceEdited,
            onCancelEdit = { state.optionsContent.eventSink(InstanceOptionsEvent.OnCancelEditClick) },
            onSaveInstance = { state.optionsContent.eventSink(InstanceOptionsEvent.OnSaveClick) },
        )
        AppTabGroup(
            selectedTab = state.selectedTab,
            onSelectedTabChange = { eventSink(InstanceDetailsEvent.OnTabClick(it)) },
            tabs = InstanceDetailsTab.entries.filter {
                (it != InstanceDetailsTab.Advanced || state.optionsContent.state.showNativeLibraryOptions) &&
                        (it != InstanceDetailsTab.Logs ||
                                state.logsContent.state.logs != null ||
                                state.selectedTab == InstanceDetailsTab.Logs)
            },
            orientation = if (state.horizontalTabs) Orientation.Horizontal else Orientation.Vertical,
            tabTitle = { tab -> InstanceDetailsTabTitle(tab, state) }
        ) { tab -> InstanceDetailsTabContent(tab, instance, state) }
    }
}

@Composable
private fun AppTabScope.InstanceDetailsTabTitle(tab: InstanceDetailsTab, state: InstanceDetailsState) {
    val badgeCount = when (tab) {
        InstanceDetailsTab.Mods -> state.modsContent.state.files?.count { it.enabled }
        InstanceDetailsTab.ResourcePacks -> state.resourcePacksContent.state.files?.count { it.enabled }
        InstanceDetailsTab.ShaderPacks -> state.shaderPacksContent.state.files?.count { it.enabled }
        InstanceDetailsTab.Worlds -> state.worldsContent.state.worlds?.size
        InstanceDetailsTab.Servers -> state.serversContent.state.servers?.size
        else -> null
    }
    val isEdited = when (tab) {
        InstanceDetailsTab.Options -> state.optionsContent.state.let {
            it.isNameEdited || it.isGameVersionEdited || it.isModLoaderEdited || it.isCheckForUpdatesEdited
        }

        InstanceDetailsTab.Java -> state.optionsContent.state.let {
            it.isJavaOverrideEdited || it.isJvmMemoryOptionsEdited
        }

        InstanceDetailsTab.Advanced -> state.optionsContent.state.isNativeLibraryOptionsEdited
        else -> false
    }
    val hasUpdates = when (tab) {
        InstanceDetailsTab.Mods -> state.modsContent.state.hasUpdates
        InstanceDetailsTab.ResourcePacks -> state.resourcePacksContent.state.hasUpdates
        InstanceDetailsTab.ShaderPacks -> state.shaderPacksContent.state.hasUpdates
        else -> false
    }
    val title = buildString {
        append(tab.title)
        if (isEdited) append("*")
        if (badgeCount != null) append(" ($badgeCount)")
    }
    AppSidebarTabItem(title = title, icon = tab.icon, badgeColor = Theme[colors][primary].takeIf { hasUpdates })
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceDetailsTabContent(tab: InstanceDetailsTab, instance: InstanceView, state: InstanceDetailsState) {
    val onOpenProject: (String, String) -> Unit = { platformId, projectId ->
        eventSink(InstanceDetailsEvent.OnOpenProjectClick(platformId, projectId))
    }
    val onOpenFolder: (InstanceFolder) -> Unit = {
        state.header.eventSink(InstanceHeaderEvent.OnOpenInstanceFolderClick(it))
    }
    when (tab) {
        InstanceDetailsTab.Options -> InstanceOptions(instance, state.optionsContent, state.horizontalTabs)
        InstanceDetailsTab.Java -> InstanceJavaOptions(instance, state.optionsContent, state.horizontalTabs)
        InstanceDetailsTab.Advanced -> InstanceAdvancedOptions(instance, state.optionsContent, state.horizontalTabs)
        InstanceDetailsTab.Mods -> InstalledMods(
            instance = instance,
            content = state.modsContent,
            horizontalTabs = state.horizontalTabs,
            onAddClick = { eventSink(InstanceDetailsEvent.OnAddContentClick(ProjectType.Mod)) },
            onOpenProject = onOpenProject,
            onOpenFolder = onOpenFolder,
        )

        InstanceDetailsTab.ResourcePacks -> InstalledResourcePacks(
            content = state.resourcePacksContent,
            horizontalTabs = state.horizontalTabs,
            onAddClick = { eventSink(InstanceDetailsEvent.OnAddContentClick(ProjectType.ResourcePack)) },
            onOpenProject = onOpenProject,
            onOpenFolder = onOpenFolder,
        )

        InstanceDetailsTab.ShaderPacks -> InstalledShaderPacks(
            content = state.shaderPacksContent,
            horizontalTabs = state.horizontalTabs,
            onAddClick = { eventSink(InstanceDetailsEvent.OnAddContentClick(ProjectType.ShaderPack)) },
            onOpenProject = onOpenProject,
            onOpenFolder = onOpenFolder,
        )

        InstanceDetailsTab.Worlds -> InstalledWorlds(
            instance = instance,
            content = state.worldsContent,
            horizontalTabs = state.horizontalTabs,
            onAddClick = { eventSink(InstanceDetailsEvent.OnAddContentClick(ProjectType.World)) },
            onOpenFolder = { onOpenFolder(InstanceFolder.Saves) },
            onQuickJoinClick = {
                state.launch.eventSink(InstanceLaunchEvent.OnQuickJoinClick(QuickPlayTarget.Singleplayer(it)))
            },
        )

        InstanceDetailsTab.Servers -> InstanceServers(
            instance = instance,
            content = state.serversContent,
            horizontalTabs = state.horizontalTabs,
            onQuickJoinClick = {
                state.launch.eventSink(InstanceLaunchEvent.OnQuickJoinClick(QuickPlayTarget.Multiplayer(it)))
            },
        )

        InstanceDetailsTab.Logs -> InstanceLogs(state.logsContent, state.horizontalTabs)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceHeader(
    instance: InstanceView,
    state: InstanceDetailsState,
    isInstanceEdited: Boolean,
    onCancelEdit: () -> Unit,
    onSaveInstance: () -> Unit,
) = SharedElementTransitionScope {
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .appCard()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .instanceSharedElement(instance.id, "card"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InstanceHeaderIcon(instance, state.header.eventSink)
        InstanceHeaderInfo(instance)
        InstanceHeaderStatus(instance)
        InstanceHeaderEditActions(isInstanceEdited, onCancelEdit, onSaveInstance)
        InstanceHeaderUpdateButton(instance, state.update, isInstanceEdited)
        InstanceHeaderActionButton(instance, state.launch.eventSink, state.installation.eventSink, isInstanceEdited)
        InstanceHeaderMenu(instance, state)
    }
}

@Composable
context(_: SharedElementTransitionScope)
private fun InstanceHeaderIcon(instance: InstanceView, headerSink: EventSink<InstanceHeaderEvent>) {
    val pickerLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let { headerSink(InstanceHeaderEvent.OnIconSelected(it.path)) }
    }
    InstanceIcon(
        instanceId = instance.id,
        name = instance.name,
        iconFilePath = instance.iconFilePath,
        updatedAt = instance.updatedAt,
        status = instance.status,
        size = 48.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).clickable(onClick = pickerLauncher::launch),
    )
}

@Composable
context(_: SharedElementTransitionScope)
private fun RowScope.InstanceHeaderInfo(instance: InstanceView) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppText(
            text = instance.name,
            fontWeight = FontWeight.SemiBold,
            singleLine = true,
            modifier = Modifier.instanceSharedElement(instance.id, "title")
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            InstanceHeaderInfoSection {
                AppIcon(
                    imageVector = when (instance.type) {
                        "Modpack" -> Lucide.PackageOpen
                        else -> Lucide.Settings
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                AppText(
                    text = instance.type,
                    style = Theme[textStyles][label],
                    modifier = Modifier.instanceSharedElement(instance.id, "type")
                )
            }

            InstanceHeaderInfoSection {
                instance.loaderType?.icon?.let {
                    AppIcon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
                AppText(
                    text = instance.loader,
                    style = Theme[textStyles][label],
                    modifier = Modifier.instanceSharedElement(instance.id, "loader")
                )
                AppText(
                    text = instance.gameVersion,
                    style = Theme[textStyles][label],
                    modifier = Modifier.instanceSharedElement(instance.id, "version")
                )
            }

            InstanceHeaderInfoSection {
                AppIcon(
                    imageVector = Lucide.Clock,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                AppText(
                    text = stringResource(InstanceRes.string.playedFor, HumanReadable.duration(instance.playTime)),
                    style = Theme[textStyles][label],
                )
            }

            InstanceHeaderInfoSection {
                AppIcon(
                    imageVector = Lucide.History,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                AppText(
                    text = instance.lastPlayedAt?.let {
                        stringResource(
                            InstanceRes.string.lastPlayed,
                            HumanReadable.timeAgo(it)
                        )
                    } ?: stringResource(InstanceRes.string.neverPlayed),
                    style = Theme[textStyles][label],
                )
            }
        }
    }
}

@Composable
private fun InstanceHeaderInfoSection(content: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
private fun InstanceHeaderStatus(instance: InstanceView) {
    if (instance.status !is InstanceStatus.Launching && instance.status !is InstanceStatus.Installing) return

    Column(
        modifier = Modifier.width(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        AppText(
            text = instance.status.text
        )
        instance.status.progress?.let {
            AppDownloadProgress(
                progress = it,
                download = instance.status.download,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InstanceHeaderEditActions(
    isInstanceEdited: Boolean,
    onCancelEdit: () -> Unit,
    onSaveInstance: () -> Unit,
) {
    AppAnimatedVisibility(isInstanceEdited) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = stringResource(InstanceRes.string.changes),
            )

            AppIconTextButton(
                onClick = onCancelEdit,
                text = stringResource(SharedRes.string.cancel),
                contentColor = Theme[colors][onBackground],
                imageVector = Lucide.X,
            )

            AppIconTextButton(
                onClick = onSaveInstance,
                text = stringResource(SharedRes.string.save),
                backgroundColor = Theme[colors][success],
                contentColor = Theme[colors][onSuccess],
                imageVector = Lucide.Save,
            )
        }
    }
}

@Composable
private fun InstanceHeaderUpdateButton(
    instance: InstanceView,
    update: Store<InstanceUpdateState, InstanceUpdateEvent>,
    isInstanceEdited: Boolean
) {
    val canUpdate = instance.status is InstanceStatus.Idle || instance.status is InstanceStatus.Failed
    AppAnimatedVisibility(update.state.modpackUpdate != null && canUpdate && !isInstanceEdited) {
        AppIconTextButton(
            onClick = { update.eventSink(InstanceUpdateEvent.OnUpdateModpackClick) },
            text = stringResource(InstanceRes.string.updateModpack),
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary],
            imageVector = Lucide.Download,
        )
    }
}

@Composable
private fun InstanceHeaderActionButton(
    instance: InstanceView,
    launchSink: EventSink<InstanceLaunchEvent>,
    installationSink: EventSink<InstanceInstallationEvent>,
    isInstanceEdited: Boolean,
) {
    AppAnimatedVisibility(!isInstanceEdited) {
        when (instance.status) {
            is InstanceStatus.Installing.AwaitingManualDownloads -> AppIconTextButton(
                onClick = { installationSink(InstanceInstallationEvent.OnFinishInstallationClick) },
                text = stringResource(InstanceRes.string.finishInstallation),
                backgroundColor = Theme[colors][primary],
                contentColor = Theme[colors][onPrimary],
                imageVector = Lucide.Download,
            )

            is InstanceStatus.Installing.Interrupted -> AppIconTextButton(
                onClick = { installationSink(InstanceInstallationEvent.OnResumeInstallationClick) },
                text = stringResource(InstanceRes.string.resumeInstallation),
                backgroundColor = Theme[colors][primary],
                contentColor = Theme[colors][onPrimary],
                imageVector = Lucide.RefreshCw,
            )

            is InstanceStatus.Idle, is InstanceStatus.Failed ->
                InstanceLaunchButton(onClick = { launchSink(InstanceLaunchEvent.OnLaunchClick) })

            is InstanceStatus.Launching -> AppIconTextButton(
                onClick = { launchSink(InstanceLaunchEvent.OnCancelClick) },
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

            is InstanceStatus.Running -> InstanceStopButton(onClick = { launchSink(InstanceLaunchEvent.OnStopClick) })

            is InstanceStatus.Stopping -> InstanceKillButton(onClick = { launchSink(InstanceLaunchEvent.OnKillClick) })
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
private fun InstanceHeaderMenu(instance: InstanceView, state: InstanceDetailsState) {
    val headerSink = state.header.eventSink
    val packPickerLauncher = rememberFilePickerLauncher(type = FileKitType.File("zip", "mrpack", "vcpack")) { file ->
        file?.let { state.update.eventSink(InstanceUpdateEvent.OnUpdateFromFileSelected(it.path)) }
    }
    AppMenuButton {
        if (instance.status is InstanceStatus.Installing) {
            AppMenuItem(
                onClick = { state.launch.eventSink(InstanceLaunchEvent.OnCancelClick) },
                text = stringResource(InstanceRes.string.cancelSetup),
                imageVector = Lucide.X,
                contentColor = Theme[colors][error]
            )
        }
        AppMenuItem(
            onClick = { headerSink(InstanceHeaderEvent.OnOpenInstanceFolderClick(InstanceFolder.Root)) },
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
                onClick = { headerSink(InstanceHeaderEvent.OnRemoveIconClick) },
                text = stringResource(InstanceRes.string.removeIcon),
                imageVector = Lucide.ImageOff
            )
        } else if (instance.modpackInfo != null) {
            AppMenuItem(
                onClick = { headerSink(InstanceHeaderEvent.OnRestoreIconClick) },
                text = stringResource(InstanceRes.string.restoreIcon),
                imageVector = Lucide.Image
            )
        }
        if (instance.status is InstanceStatus.Idle || instance.status is InstanceStatus.Failed) {
            AppMenuItem(
                onClick = { headerSink(InstanceHeaderEvent.OnCopyClick(instance.name)) },
                text = stringResource(InstanceRes.string.copyInstance),
                imageVector = Lucide.Copy
            )
            AppMenuItem(
                onClick = { headerSink(InstanceHeaderEvent.OnExportClick(instance.name)) },
                text = stringResource(InstanceRes.string.exportInstance),
                imageVector = Lucide.Share
            )
            AppMenuItem(
                onClick = packPickerLauncher::launch,
                text = stringResource(InstanceRes.string.updateFromFile),
                imageVector = Lucide.FileUp
            )
            AppMenuItem(
                onClick = { headerSink(InstanceHeaderEvent.OnCopyOptionsClick(instance.gameVersion)) },
                text = stringResource(InstanceRes.string.copyOptionsFrom),
                imageVector = Lucide.Keyboard
            )
            AppMenuItem(
                onClick = { headerSink(InstanceHeaderEvent.OnSaveDefaultOptionsClick) },
                text = stringResource(InstanceRes.string.saveOptionsAsDefault),
                imageVector = Lucide.BookmarkCheck
            )
        }
        AppMenuItem(
            onClick = { headerSink(InstanceHeaderEvent.OnDeleteClick) },
            text = stringResource(SharedRes.string.delete),
            imageVector = Lucide.Trash,
            contentColor = Theme[colors][error]
        )
    }
}

private val InstanceDetailsTab.icon: ImageVector
    @Stable get() = when (this) {
        InstanceDetailsTab.Logs -> Lucide.Logs
        InstanceDetailsTab.Options -> Lucide.Settings2
        InstanceDetailsTab.Java -> Lucide.Coffee
        InstanceDetailsTab.Advanced -> Lucide.Wrench
        InstanceDetailsTab.Mods -> Lucide.Box
        InstanceDetailsTab.ResourcePacks -> Lucide.Paintbrush
        InstanceDetailsTab.ShaderPacks -> Lucide.Glasses
        InstanceDetailsTab.Worlds -> Lucide.Earth
        InstanceDetailsTab.Servers -> Lucide.Server
    }

private val InstanceDetailsTab.title: String
    @Composable get() = when (this) {
        InstanceDetailsTab.Logs -> stringResource(InstanceRes.string.logs)
        InstanceDetailsTab.Options -> stringResource(InstanceRes.string.options)
        InstanceDetailsTab.Java -> stringResource(SharedRes.string.java)
        InstanceDetailsTab.Advanced -> stringResource(SharedRes.string.advanced)
        InstanceDetailsTab.Mods -> stringResource(SharedRes.string.mods)
        InstanceDetailsTab.ResourcePacks -> stringResource(SharedRes.string.resourcePacks)
        InstanceDetailsTab.ShaderPacks -> stringResource(SharedRes.string.shaderPacks)
        InstanceDetailsTab.Worlds -> stringResource(InstanceRes.string.worlds)
        InstanceDetailsTab.Servers -> stringResource(InstanceRes.string.servers)
    }
