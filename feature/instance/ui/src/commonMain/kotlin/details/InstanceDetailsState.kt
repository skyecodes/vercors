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
import app.vercors.feature.project.domain.model.pack.InstalledPack
import app.vercors.feature.project.domain.model.pack.LocalPack
import app.vercors.feature.project.domain.model.pack.matches
import app.vercors.feature.project.domain.model.update.ModpackUpdate
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
    val modpackUpdate: ModpackUpdateView?,
    val promptModpackUpdate: Boolean,
    val localPackUpdatePrompt: LocalPackUpdatePrompt? = null,
)

@Immutable
enum class LocalPackUpdateKind {
    SamePack,
    NoRecord,
    OtherPack,
}

@Immutable
data class LocalPackUpdatePrompt(
    val pack: LocalPack,
    val currentLabel: String,
    val newLabel: String,
    val gameVersion: String,
    val kind: LocalPackUpdateKind,
)

fun LocalPack.toUpdatePrompt(installedPack: InstalledPack?): LocalPackUpdatePrompt = LocalPackUpdatePrompt(
    pack = this,
    currentLabel = installedPack?.let { packLabel(it.name, it.versionName) }.orEmpty(),
    newLabel = packLabel(name, versionName),
    gameVersion = gameVersion,
    kind = when {
        installedPack == null -> LocalPackUpdateKind.NoRecord
        installedPack.matches(this) -> LocalPackUpdateKind.SamePack
        else -> LocalPackUpdateKind.OtherPack
    }
)

// A platform version name often already contains the pack name ("Fabulously Optimized 6.4.0")
private fun packLabel(name: String, versionName: String?): String = when {
    versionName == null -> name
    versionName.contains(name, ignoreCase = true) -> versionName
    else -> "$name $versionName"
}

@Immutable
data class ModpackUpdateView(
    val currentVersion: String,
    val latestVersion: String,
    val newGameVersion: String?,
)

fun ModpackUpdate.toView(gameVersion: String): ModpackUpdateView = ModpackUpdateView(
    currentVersion = current.displayName,
    latestVersion = latest.displayName,
    newGameVersion = latest.gameVersions.takeIf { it.isNotEmpty() && gameVersion !in it }?.first(),
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
    val modpackVersion: String?,
    val pendingManualDownloads: List<PendingManualDownload>,
    val iconFilePath: String?,
    val javaInstallationId: String?,
    val jvmMemoryOptions: JvmMemoryOptions,
    val nativeLibraryOptions: NativeLibraryOptions,
    val checkForUpdates: Boolean?,
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
    modpackVersion = instance.modpackInfo?.versionName,
    pendingManualDownloads = instance.pendingManualDownloads,
    iconFilePath = instance.iconFilePath,
    javaInstallationId = instance.javaInstallationId,
    jvmMemoryOptions = instance.jvmMemoryOptions,
    nativeLibraryOptions = instance.nativeLibraryOptions,
    checkForUpdates = instance.checkForUpdates,
    updatedAt = instance.updatedAt,
    playTime = instance.playTime,
    lastPlayedAt = instance.lastPlayedAt
)

val Instance.displayType: String get() = if (modpackInfo != null) "Modpack" else "Custom"
