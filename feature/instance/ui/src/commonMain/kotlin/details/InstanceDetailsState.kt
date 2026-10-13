package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.feature.account.ui.list.LoginDialog
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.displayName
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.ui.copy.CopyInstanceDialog
import app.vercors.feature.instance.ui.details.content.InstalledContentEvent
import app.vercors.feature.instance.ui.details.content.InstalledContentState
import app.vercors.feature.instance.ui.details.logs.InstanceLogsEvent
import app.vercors.feature.instance.ui.details.logs.InstanceLogsState
import app.vercors.feature.instance.ui.details.options.InstanceOptionsEvent
import app.vercors.feature.instance.ui.details.options.InstanceOptionsState
import app.vercors.feature.instance.ui.details.servers.InstanceServersEvent
import app.vercors.feature.instance.ui.details.servers.InstanceServersState
import app.vercors.feature.instance.ui.details.worlds.InstalledWorldsEvent
import app.vercors.feature.instance.ui.details.worlds.InstalledWorldsState
import app.vercors.feature.instance.ui.export.ExportInstanceDialog
import app.vercors.feature.instance.ui.install.FinishInstallationDialog
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.ui.Store
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.time.Duration
import kotlin.time.Instant

@Immutable
data class InstanceDetailsState(
    val instance: ProgressiveOutcome<InstanceError, InstanceView>,
    val promptInstallJava: Int?,
    val selectedTab: InstanceDetailsTab,
    val horizontalTabs: Boolean,
    val promptDeleteConfirmation: Boolean,
    val copyInstanceDialog: CopyInstanceDialog?,
    val exportInstanceDialog: ExportInstanceDialog?,
    val optionsContent: Store<InstanceOptionsState, InstanceOptionsEvent>,
    val modsContent: Store<InstalledContentState, InstalledContentEvent>,
    val resourcePacksContent: Store<InstalledContentState, InstalledContentEvent>,
    val shaderPacksContent: Store<InstalledContentState, InstalledContentEvent>,
    val worldsContent: Store<InstalledWorldsState, InstalledWorldsEvent>,
    val serversContent: Store<InstanceServersState, InstanceServersEvent>,
    val logsContent: Store<InstanceLogsState, InstanceLogsEvent>,
    val promptFinishInstallation: Boolean,
    val finishInstallationDialog: FinishInstallationDialog,
    val promptLogin: Boolean = false,
    val loginDialog: LoginDialog,
    val promptJavaInstallProgress: Boolean = false,
    val installJavaProgressDialog: InstallJavaProgressDialog,
)

@Immutable
data class InstanceView(
    val id: InstanceId,
    val name: String,
    val gameVersion: String,
    val loader: String,
    val loaderType: ModLoaderType?,
    val loaderVersion: String?,
    val type: String,
    val status: InstanceStatus,
    val modpackInfo: InstanceModpackInfo?,
    val pendingManualDownloads: List<PendingManualDownload>,
    val iconFilePath: String?,
    val javaInstallationId: String?,
    val jvmMemoryOptions: JvmMemoryOptions,
    val nativeLibraryOptions: NativeLibraryOptions,
    val updatedAt: Instant,
    val playTime: Duration,
    val lastPlayedAt: Instant? = null
)

fun InstanceAndStatus.toView(): InstanceView = InstanceView(
    id = instance.id,
    name = instance.name,
    gameVersion = instance.gameVersion,
    loader = instance.modLoader?.type.displayName,
    loaderType = instance.modLoader?.type,
    loaderVersion = instance.modLoader?.version,
    type = instance.displayType,
    status = status,
    modpackInfo = instance.modpackInfo,
    pendingManualDownloads = instance.pendingManualDownloads,
    iconFilePath = instance.iconFilePath,
    javaInstallationId = instance.javaInstallationId,
    jvmMemoryOptions = instance.jvmMemoryOptions,
    nativeLibraryOptions = instance.nativeLibraryOptions,
    updatedAt = instance.updatedAt,
    playTime = instance.playTime,
    lastPlayedAt = instance.lastPlayedAt
)

val Instance.displayType: String get() = if (modpackInfo != null) "Modpack" else "Custom"
