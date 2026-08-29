package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import app.vercors.feature.account.ui.list.LoginDialog
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.displayName
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.ui.install.FinishInstallationDialog
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.ui.Store
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.time.Instant

@Immutable
data class InstanceDetailsState(
    val instance: ProgressiveOutcome<InstanceError, InstanceView>,
    val promptInstallJava: Int?,
    val selectedTab: InstanceDetailsTab,
    val promptDeleteConfirmation: Boolean,
    val optionsContent: Store<InstanceOptionsState, InstanceOptionsEvent>,
    val modsContent: Store<InstalledContentState, InstalledContentEvent>,
    val resourcePacksContent: Store<InstalledContentState, InstalledContentEvent>,
    val shaderPacksContent: Store<InstalledContentState, InstalledContentEvent>,
    val logsContent: Store<InstanceLogsState, InstanceLogsEvent>,
    val promptFinishInstallation: Boolean,
    val finishInstallationDialog: FinishInstallationDialog,
    val promptLogin: Boolean = false,
    val loginDialog: LoginDialog,
    val promptJavaInstall: Boolean = false,
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
    /** Bumped whenever the icon changes — used to bust the image cache when [iconFilePath] is reused unchanged. */
    val updatedAt: Instant,
)

@Immutable
data class InstanceLogEventView(
    val timestamp: Instant,
    val level: Level,
    val message: String,
    val isLauncher: Boolean,
) {
    enum class Level {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }
}

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
    updatedAt = instance.updatedAt
)

val Instance.displayType: String get() = if (modpackInfo != null) "Modpack" else "Custom"

@Immutable
data class InstalledContentFileView(
    val fileName: String,
    val title: String,
    val description: String?,
    val version: String?,
    val iconFilePath: String?,
    val enabled: Boolean,
    val providers: List<ModProvider>,
    val lastModified: Instant?,
)

fun InstalledContentFile.toView(): InstalledContentFileView = InstalledContentFileView(
    fileName = fileName,
    title = title,
    description = description,
    version = version,
    iconFilePath = iconFilePath,
    enabled = enabled,
    providers = providers,
    lastModified = lastModified
)

@Immutable
data class JavaInstallProgressView(
    val majorVersion: Int = 0,
    val progress: Float? = null,
    val done: Boolean = false,
    val error: (@Composable () -> String)? = null,
)

fun InstanceLogEvent.toView(): InstanceLogEventView = when (this) {
    is InstanceLogEvent.GameLog4j -> InstanceLogEventView(
        timestamp = inst,
        level = InstanceLogEventView.Level.valueOf(level.uppercase()),
        message = toString(),
        isLauncher = false
    )

    is InstanceLogEvent.Plain -> InstanceLogEventView(
        timestamp = inst,
        level = if (this is InstanceLogEvent.Error) InstanceLogEventView.Level.ERROR else InstanceLogEventView.Level.INFO,
        message = toString(),
        isLauncher = this is InstanceLogEvent.Launcher
    )
}
