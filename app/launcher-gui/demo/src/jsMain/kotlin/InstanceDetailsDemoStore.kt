package app.vercors.launcher.demo

import androidx.compose.runtime.mutableStateOf
import app.vercors.feature.account.ui.list.LoginDialog
import app.vercors.feature.account.ui.list.LoginStateView
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.ui.details.*
import app.vercors.feature.instance.ui.details.content.InstalledContentEvent
import app.vercors.feature.instance.ui.details.content.InstalledContentFileView
import app.vercors.feature.instance.ui.details.content.InstalledContentState
import app.vercors.feature.instance.ui.details.logs.InstanceLogsState
import app.vercors.feature.instance.ui.details.options.InstanceEditState
import app.vercors.feature.instance.ui.details.options.InstanceOptionsState
import app.vercors.feature.instance.ui.details.servers.InstanceServersState
import app.vercors.feature.instance.ui.details.worlds.InstalledWorldsState
import app.vercors.feature.instance.ui.install.FinishInstallationDialog
import app.vercors.launcher.demo.data.CURSEFORGE_PLATFORM_ID
import app.vercors.launcher.demo.data.MONIFACTORY_ID
import app.vercors.launcher.demo.data.MONIFACTORY_MODS
import app.vercors.launcher.demo.data.toView
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.ui.Store
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private const val TOTAL_MEMORY_MB = 32768
private const val REQUIRED_JAVA_MAJOR_VERSION = 17

private val MONIFACTORY_RESOURCE_PACKS = listOf(
    InstalledContentFileView(
        fileName = "EIO Texture Backport.zip",
        title = "EIO Texture Backport",
        description = null,
        version = null,
        iconFilePath = null,
        enabled = true,
        providers = emptyList(),
        lastModified = null,
    ),
)

fun instanceDetailsDemoStore(): Store<InstanceDetailsState, InstanceDetailsEvent> {
    val instance = monifactoryView()
    return Store(
        state = InstanceDetailsState(
            instance = ProgressiveOutcome.Success(instance),
            promptInstallJava = null,
            selectedTab = InstanceDetailsTab.Mods,
            horizontalTabs = false,
            promptDeleteConfirmation = false,
            copyInstanceDialog = null,
            exportInstanceDialog = null,
            optionsContent = Store(optionsState(instance)) {},
            modsContent = contentStore(MONIFACTORY_MODS.map { it.toView() }),
            resourcePacksContent = contentStore(MONIFACTORY_RESOURCE_PACKS),
            shaderPacksContent = contentStore(emptyList()),
            worldsContent = Store(InstalledWorldsState(worlds = emptyList(), promptRemoveConfirmation = null)) {},
            serversContent = Store(InstanceServersState(servers = emptyList())) {},
            logsContent = Store(InstanceLogsState(logs = emptyList())) {},
            promptFinishInstallation = false,
            finishInstallationDialog = FinishInstallationDialog(mutableStateOf(emptyList())) {},
            loginDialog = LoginDialog(mutableStateOf(LoginStateView())) {},
            installJavaProgressDialog = InstallJavaProgressDialog(mutableStateOf(JavaInstallProgressView())),
        ),
        eventSink = {},
    )
}

private fun monifactoryView(): InstanceView {
    val now = Clock.System.now()
    return InstanceView(
        id = MONIFACTORY_ID,
        name = "Monifactory",
        gameVersion = "1.20.1",
        loader = "Forge",
        loaderType = ModLoaderType.Forge,
        loaderVersion = "47.4.10",
        type = "Modpack",
        status = InstanceStatus.Idle,
        modpackInfo = InstanceModpackInfo(
            platformId = CURSEFORGE_PLATFORM_ID,
            projectId = MONIFACTORY_ID,
            fileId = MONIFACTORY_ID
        ),
        pendingManualDownloads = emptyList(),
        iconFilePath = "/icons/$MONIFACTORY_ID.webp",
        javaInstallationId = null,
        jvmMemoryOptions = JvmMemoryOptions(),
        nativeLibraryOptions = NativeLibraryOptions(),
        updatedAt = now,
        playTime = 37.hours + 42.minutes,
        lastPlayedAt = now - 1.days,
    )
}

private fun optionsState(instance: InstanceView): InstanceOptionsState = InstanceOptionsState(
    editedInstance = InstanceEditState(),
    gameVersionList = emptyList(),
    loaderList = emptyList(),
    loaderVersionList = emptyList(),
    availableJavaInstallations = emptyList(),
    totalMemoryMb = TOTAL_MEMORY_MB,
    showNativeLibraryOptions = false,
    detectedGlfwPath = null,
    detectedOpenAlPath = null,
    globalJvmMemoryOptions = JvmMemoryOptions(),
    globalNativeLibraryOptions = NativeLibraryOptions(),
    inheritedJavaInstallation = null,
    requiredJavaMajorVersion = REQUIRED_JAVA_MAJOR_VERSION,
    persistedInstance = instance,
)

private fun contentStore(files: List<InstalledContentFileView>): Store<InstalledContentState, InstalledContentEvent> =
    Store(InstalledContentState(files = files, promptRemoveConfirmation = null)) {}
