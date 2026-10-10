package app.vercors.feature.config.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import app.vercors.core.domain.AppConstants
import app.vercors.core.system.OsType
import app.vercors.core.system.SystemService
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.core.ui.state.produceProgressiveState
import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.model.UpdateChannel
import app.vercors.feature.config.domain.model.withoutJavaInstallations
import app.vercors.feature.config.domain.usecase.*
import app.vercors.feature.config.ui.datadirectory.ChangeDataDirectoryDialog
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.java.domain.model.JavaInstallationError
import app.vercors.feature.java.domain.usecase.AddJavaInstallationUseCase
import app.vercors.feature.java.domain.usecase.ObserveJavaInstallationsUseCase
import app.vercors.feature.java.domain.usecase.ScanJavaInstallationsUseCase
import app.vercors.feature.project.domain.model.PlatformError
import app.vercors.feature.project.domain.model.ProjectPlatform
import app.vercors.feature.project.domain.usecase.GetPlatformListUseCase
import app.vercors.feature.theme.domain.model.ThemeDefinition
import app.vercors.feature.theme.domain.model.ThemeNamedColor
import app.vercors.feature.theme.domain.usecase.ObserveThemesUseCase
import app.vercors.shared.data.AppPaths
import app.vercors.shared.domain.model.DetectedNativeLibraries
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.domain.usecase.DetectNativeLibrariesUseCase
import app.vercors.shared.domain.usecase.GetTotalPhysicalMemoryMbUseCase
import app.vercors.shared.ui.*
import arrow.core.raise.context.either
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.collectAsRetainedState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.value
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@CircuitInject(ConfigurationScreen::class, AppScope::class)
@Inject
class ConfigurationPresenter(
    private val observeThemes: ObserveThemesUseCase,
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
    private val getPlatformList: GetPlatformListUseCase,
    private val observeJavaInstallations: ObserveJavaInstallationsUseCase,
    private val scanJavaInstallations: ScanJavaInstallationsUseCase,
    private val addJavaInstallation: AddJavaInstallationUseCase,
    private val snackbarService: SnackbarService,
    private val getTotalPhysicalMemoryMb: GetTotalPhysicalMemoryMbUseCase,
    private val detectNativeLibraries: DetectNativeLibrariesUseCase,
    private val systemService: SystemService,
    private val getContentCacheSize: GetContentCacheSizeUseCase,
    private val clearContentCache: ClearContentCacheUseCase,
    private val appPaths: AppPaths,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val changeDataDirectoryDialogFactory: ChangeDataDirectoryDialog.Factory,
    private val observeLauncherUpdateState: ObserveLauncherUpdateStateUseCase,
    private val checkForLauncherUpdate: CheckForLauncherUpdateUseCase,
    private val downloadLauncherUpdate: DownloadLauncherUpdateUseCase,
    private val installLauncherUpdate: InstallLauncherUpdateUseCase,
    @Named(AppConstants.APP_NAME_KEY) private val appName: String,
    @Named(AppConstants.APP_VERSION_KEY) private val appVersion: String,
) : Presenter<ConfigurationState, ConfigurationEvent> {
    @Composable
    override fun present(): Store<ConfigurationState, ConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val (selectedTab, setSelectedTab) = rememberSaveable { mutableStateOf(ConfigurationTab.General) }
        val themeList by observeThemeViewList()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val javaInstallations by observeJavaInstallations().collectProgressiveAsState()
        val platformList by observePlatformList()
        val (javaAddError, setJavaAddError) = remember { mutableStateOf<String?>(null) }
        val (javaScanInProgress, setJavaScanInProgress) = remember { mutableStateOf(false) }
        val totalMemoryMb = remember { getTotalPhysicalMemoryMb() }
        val showNativeLibraryOptions = systemService.osType == OsType.Linux
        val detectedNativeLibraries by produceState<DetectedNativeLibraries?>(null, showNativeLibraryOptions) {
            if (showNativeLibraryOptions) value = detectNativeLibraries()
        }
        val (contentCacheSizeBytes, setContentCacheSizeBytes) = remember { mutableStateOf<Long?>(null) }
        LaunchedEffect(Unit) { setContentCacheSizeBytes(getContentCacheSize()) }
        val (promptClearContentCache, setPromptClearContentCache) = remember { mutableStateOf(false) }
        val dataDirectory = remember { appPaths.root.toString() }
        val instances by observeInstanceList().collectProgressiveAsState()
        val hasRunningInstance = instances.valueOrNull?.any { it.getOrNull()?.status is InstanceStatus.Running } == true
        val (promptChangeDataDirectory, setPromptChangeDataDirectory) = remember { mutableStateOf(false) }
        val changeDataDirectoryDialog =
            remember(dataDirectory) { changeDataDirectoryDialogFactory.create(dataDirectory) }
        val launcherUpdate by observeLauncherUpdateState().collectAsState()
        val (promptRestartForUpdate, setPromptRestartForUpdate) = remember { mutableStateOf(false) }
        val cfg = configuration.valueOrNull
        val selectedTheme = themeList.find { it.id == cfg?.selectedThemeId }
        val selectedAccent = selectedTheme?.accents?.find { it.id == cfg?.selectedAccent }
        val jvmMemoryOptions = cfg?.jvmMemoryOptions ?: JvmMemoryOptions()
        val nativeLibraryOptions = cfg?.nativeLibraryOptions ?: NativeLibraryOptions()

        return ConfigurationState(
            selectedTab = selectedTab,
            selectedTheme = selectedTheme,
            themeList = themeList,
            selectedAccent = selectedAccent,
            animations = cfg?.animations,
            uiScalePercent = cfg?.uiScalePercent,
            horizontalTabs = cfg?.horizontalTabs,
            metaApiUrl = cfg?.metaApiUrl,
            networkTimeoutSeconds = cfg?.networkTimeout?.inWholeSeconds?.toInt(),
            parallelDownloads = cfg?.parallelDownloads,
            parallelFilesystemOperations = cfg?.parallelFilesystemOperations,
            preferredPlatform = cfg?.preferredPlatform,
            platformList = platformList.valueOrNull ?: emptyList(),
            startupScreen = cfg?.startupScreen,
            homeCategories = cfg?.homeCategories ?: HomeCategoryConfig.entries,
            javaInstallations = javaInstallations.valueOrNull ?: emptyList(),
            preferredJavaByMajorVersion = cfg?.preferredJavaByMajorVersion ?: emptyMap(),
            javaAddError = javaAddError,
            javaScanInProgress = javaScanInProgress,
            jvmMemoryOptions = jvmMemoryOptions,
            totalMemoryMb = totalMemoryMb,
            nativeLibraryOptions = nativeLibraryOptions,
            showNativeLibraryOptions = showNativeLibraryOptions,
            detectedGlfwPath = detectedNativeLibraries?.glfwPath,
            detectedOpenAlPath = detectedNativeLibraries?.openAlPath,
            contentCacheSizeBytes = contentCacheSizeBytes,
            promptClearContentCache = promptClearContentCache,
            dataDirectory = dataDirectory,
            canChangeDataDirectory = !hasRunningInstance,
            promptChangeDataDirectory = promptChangeDataDirectory,
            changeDataDirectoryDialog = changeDataDirectoryDialog,
            appName = appName,
            appVersion = appVersion,
            checkForUpdatesOnStart = cfg?.checkForUpdatesOnStart,
            updateChannel = cfg?.let { it.updateChannel ?: UpdateChannel.fromVersion(appVersion) },
            launcherUpdate = launcherUpdate,
            promptRestartForUpdate = promptRestartForUpdate,
        ) + { event ->
            handleEvent(
                event = event,
                presenterScope = presenterScope,
                themeList = themeList,
                setSelectedTab = setSelectedTab,
                setJavaAddError = setJavaAddError,
                javaScanInProgress = javaScanInProgress,
                setJavaScanInProgress = setJavaScanInProgress,
                setPromptClearContentCache = setPromptClearContentCache,
                setContentCacheSizeBytes = setContentCacheSizeBytes,
                dataDirectory = dataDirectory,
                setPromptChangeDataDirectory = setPromptChangeDataDirectory,
                hasRunningInstance = hasRunningInstance,
                setPromptRestartForUpdate = setPromptRestartForUpdate,
            )
        }
    }

    private fun handleEvent(
        event: ConfigurationEvent,
        presenterScope: CoroutineScope,
        themeList: List<ThemeView>,
        setSelectedTab: (ConfigurationTab) -> Unit,
        setJavaAddError: (String?) -> Unit,
        javaScanInProgress: Boolean,
        setJavaScanInProgress: (Boolean) -> Unit,
        setPromptClearContentCache: (Boolean) -> Unit,
        setContentCacheSizeBytes: (Long?) -> Unit,
        dataDirectory: String,
        setPromptChangeDataDirectory: (Boolean) -> Unit,
        hasRunningInstance: Boolean,
        setPromptRestartForUpdate: (Boolean) -> Unit,
    ) {
        when (event) {
            is ConfigurationEvent.OnTabClick -> setSelectedTab(event.tab)

            is ConfigurationEvent.OnThemeChange -> presenterScope.launch {
                selectTheme(themeList.first { t -> t.id == event.themeId })
            }

            is ConfigurationEvent.OnAccentChange -> presenterScope.launch {
                updateConfiguration { copy(selectedAccent = event.accentId) }
            }

            ConfigurationEvent.OnAnimationsToggle -> presenterScope.launch {
                updateConfiguration { copy(animations = !animations) }
            }

            ConfigurationEvent.OnHorizontalTabsToggle -> presenterScope.launch {
                updateConfiguration { copy(horizontalTabs = !horizontalTabs) }
            }

            is ConfigurationEvent.OnUiScalePercentChange -> presenterScope.launch {
                updateConfiguration { copy(uiScalePercent = event.value) }
            }

            is ConfigurationEvent.OnMetaApiUrlChange -> presenterScope.launch {
                updateConfiguration { copy(metaApiUrl = event.value) }
            }

            is ConfigurationEvent.OnNetworkTimeoutChange -> presenterScope.launch {
                updateConfiguration { copy(networkTimeout = event.value.seconds) }
            }

            is ConfigurationEvent.OnParallelDownloadsChange -> presenterScope.launch {
                updateConfiguration { copy(parallelDownloads = event.value) }
            }

            is ConfigurationEvent.OnParallelFilesystemOperationsChange -> presenterScope.launch {
                updateConfiguration { copy(parallelFilesystemOperations = event.value) }
            }

            is ConfigurationEvent.OnPreferredPlatformChange -> presenterScope.launch {
                updateConfiguration { copy(preferredPlatform = event.value) }
            }

            is ConfigurationEvent.OnStartupScreenChange -> presenterScope.launch {
                updateConfiguration { copy(startupScreen = event.value) }
            }

            is ConfigurationEvent.OnHomeCategoriesChange -> presenterScope.launch {
                updateConfiguration { copy(homeCategories = event.value) }
            }

            is ConfigurationEvent.OnJavaInstallationSelected ->
                handleAddJavaInstallation(event.path, presenterScope, setJavaAddError)

            ConfigurationEvent.OnRescanJavaClick ->
                handleRescanJavaInstallations(presenterScope, javaScanInProgress, setJavaScanInProgress)

            is ConfigurationEvent.OnPreferredJavaChange -> presenterScope.launch {
                updateConfiguration {
                    copy(
                        preferredJavaByMajorVersion = preferredJavaByMajorVersion +
                                (event.majorVersion to event.installationId)
                    )
                }
            }

            is ConfigurationEvent.OnForgetInstallation -> handleForgetInstallation(event.installationId, presenterScope)

            ConfigurationEvent.OnJavaAddErrorDismiss -> setJavaAddError(null)

            is ConfigurationEvent.OnMinMemoryMbChange -> presenterScope.launch {
                updateConfiguration { copy(jvmMemoryOptions = jvmMemoryOptions.copy(minMemoryMb = event.value)) }
            }

            is ConfigurationEvent.OnMaxMemoryMbChange -> handleSetMaxMemoryMb(event.value, presenterScope)

            is ConfigurationEvent.OnExtraJvmArgsChange -> presenterScope.launch {
                updateConfiguration { copy(jvmMemoryOptions = jvmMemoryOptions.copy(extraArgs = event.value)) }
            }

            is ConfigurationEvent.OnUseSystemGlfwToggle -> presenterScope.launch {
                updateConfiguration {
                    copy(nativeLibraryOptions = nativeLibraryOptions.copy(useSystemGlfw = event.checked))
                }
            }

            is ConfigurationEvent.OnGlfwPathChange -> presenterScope.launch {
                updateConfiguration { copy(nativeLibraryOptions = nativeLibraryOptions.copy(glfwPath = event.value)) }
            }

            is ConfigurationEvent.OnUseSystemOpenAlToggle -> presenterScope.launch {
                updateConfiguration {
                    copy(nativeLibraryOptions = nativeLibraryOptions.copy(useSystemOpenAl = event.checked))
                }
            }

            is ConfigurationEvent.OnOpenAlPathChange -> presenterScope.launch {
                updateConfiguration { copy(nativeLibraryOptions = nativeLibraryOptions.copy(openAlPath = event.value)) }
            }

            ConfigurationEvent.OnClearContentCacheClick -> setPromptClearContentCache(true)

            is ConfigurationEvent.OnConfirmClearContentCache -> handleConfirmClearContentCache(
                event = event,
                presenterScope = presenterScope,
                setPromptClearContentCache = setPromptClearContentCache,
                setContentCacheSizeBytes = setContentCacheSizeBytes,
            )

            ConfigurationEvent.OnOpenDataDirectoryClick -> handleOpenDataDirectory(dataDirectory, presenterScope)

            ConfigurationEvent.OnChangeDataDirectoryClick -> setPromptChangeDataDirectory(true)
            ConfigurationEvent.OnChangeDataDirectoryDismiss -> setPromptChangeDataDirectory(false)

            ConfigurationEvent.OnCheckForUpdatesOnStartToggle -> presenterScope.launch {
                updateConfiguration { copy(checkForUpdatesOnStart = !checkForUpdatesOnStart) }
            }

            is ConfigurationEvent.OnUpdateChannelChange -> handleUpdateChannelChange(event.value, presenterScope)

            ConfigurationEvent.OnCheckForUpdatesClick -> presenterScope.launch { checkForLauncherUpdate() }
            ConfigurationEvent.OnUpdateClick -> presenterScope.launch { downloadLauncherUpdate() }

            ConfigurationEvent.OnRestartToUpdateClick ->
                if (hasRunningInstance) setPromptRestartForUpdate(true) else installLauncherUpdate()

            is ConfigurationEvent.OnConfirmRestartForUpdate -> {
                setPromptRestartForUpdate(false)
                if (event.result) installLauncherUpdate()
            }
        }
    }

    private fun handleUpdateChannelChange(channel: UpdateChannel, presenterScope: CoroutineScope) {
        presenterScope.launch {
            updateConfiguration { copy(updateChannel = channel) }
            checkForLauncherUpdate()
        }
    }

    private fun handleOpenDataDirectory(dataDirectory: String, presenterScope: CoroutineScope) {
        presenterScope.launch {
            if (!systemService.openInFileManager(dataDirectory)) {
                snackbarService.error { stringResource(ConfigRes.string.errorOpenDataDirectoryFailed) }
            }
        }
    }

    private fun handleConfirmClearContentCache(
        event: ConfigurationEvent.OnConfirmClearContentCache,
        presenterScope: CoroutineScope,
        setPromptClearContentCache: (Boolean) -> Unit,
        setContentCacheSizeBytes: (Long?) -> Unit,
    ) {
        setPromptClearContentCache(false)
        if (!event.result) return
        presenterScope.launch {
            clearContentCache()
            setContentCacheSizeBytes(getContentCacheSize())
            snackbarService.success { stringResource(ConfigRes.string.contentCacheCleared) }
        }
    }

    private fun handleAddJavaInstallation(
        path: String,
        presenterScope: CoroutineScope,
        setJavaAddError: (String?) -> Unit,
    ) {
        presenterScope.launch {
            either { addJavaInstallation(path) }.onLeft { error ->
                setJavaAddError(
                    when (error) {
                        is JavaInstallationError.NotAJavaInstallation -> error.path
                    }
                )
            }
        }
    }

    private fun handleRescanJavaInstallations(
        presenterScope: CoroutineScope,
        javaScanInProgress: Boolean,
        setJavaScanInProgress: (Boolean) -> Unit,
    ) {
        if (javaScanInProgress) return
        setJavaScanInProgress(true)
        presenterScope.launch {
            try {
                scanJavaInstallations()
                snackbarService.success { stringResource(ConfigRes.string.javaScanCompleted) }
            } finally {
                setJavaScanInProgress(false)
            }
        }
    }

    private fun handleForgetInstallation(installationId: String, presenterScope: CoroutineScope) {
        presenterScope.launch {
            updateConfiguration { withoutJavaInstallations(setOf(installationId)) }
        }
    }

    private fun handleSetMaxMemoryMb(value: Int?, presenterScope: CoroutineScope) {
        presenterScope.launch {
            updateConfiguration {
                val newMax = value
                val clampedMin =
                    jvmMemoryOptions.minMemoryMb?.let { min -> newMax?.let { max -> minOf(min, max) } ?: min }
                copy(jvmMemoryOptions = jvmMemoryOptions.copy(maxMemoryMb = newMax, minMemoryMb = clampedMin))
            }
        }
    }

    @Composable
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    private fun observePlatformList(): State<ProgressiveOutcome<PlatformError, List<ProjectPlatform>>> =
        produceProgressiveState {
            value = progressive { getPlatformList() }
        }

    @Composable
    private fun observeThemeViewList() = observeThemes()
        .map { it.value.themes.map { theme -> theme.toView() } }
        .collectAsRetainedState(emptyList())

    private suspend fun selectTheme(theme: ThemeView) {
        updateConfiguration {
            copy(
                selectedThemeId = theme.id,
                selectedAccent = theme.accents.find { it.id == selectedAccent }?.id ?: theme.defaultAccent.id
            )
        }
    }
}

private fun ThemeDefinition.toView() = ThemeView(
    id = id,
    displayName = displayName,
    accents = accents.map { it.toView() },
    defaultAccent = accents.first { it.id == defaultAccent }.toView()
)

private fun ThemeNamedColor.toView() = ThemeNamedColorView(
    id = id,
    name = name,
    color = Color(color.value)
)
