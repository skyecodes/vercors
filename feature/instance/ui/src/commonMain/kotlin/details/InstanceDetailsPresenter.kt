package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import app.vercors.core.domain.toggle
import app.vercors.core.system.OsType
import app.vercors.core.system.SystemService
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.core.ui.state.update
import app.vercors.feature.account.domain.service.LoginState
import app.vercors.feature.account.domain.usecase.LoginUseCase
import app.vercors.feature.account.ui.list.LoginDialog
import app.vercors.feature.account.ui.list.LoginStateView
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.feature.game.domain.model.*
import app.vercors.feature.game.domain.usecase.*
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.game.ui.toView
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.install.*
import app.vercors.feature.instance.ui.list.InstanceListScreen
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.usecase.CancelInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.KillInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.StopInstanceUseCase
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.from
import app.vercors.feature.project.domain.usecase.AwaitManualDownloadUseCase
import app.vercors.feature.project.domain.usecase.GetProjectUseCase
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.domain.model.NativeLibraryType
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.domain.usecase.DetectNativeLibraryPathUseCase
import app.vercors.shared.domain.usecase.GetTotalPhysicalMemoryUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import arrow.core.raise.recover
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@AssistedInject
class InstanceDetailsPresenter(
    @Assisted private val screen: InstanceDetailsScreen,
    @Assisted private val navigator: Navigator,
    private val globalScope: CoroutineScope,
    private val observeInstance: ObserveInstanceUseCase,
    private val observeInstanceLogs: ObserveInstanceLogsUseCase,
    private val observeInstalledMods: ObserveInstalledModsUseCase,
    private val setModEnabled: SetModEnabledUseCase,
    private val removeMod: RemoveModUseCase,
    private val observeInstalledResourcePacks: ObserveInstalledResourcePacksUseCase,
    private val setResourcePackEnabled: SetResourcePackEnabledUseCase,
    private val removeResourcePack: RemoveResourcePackUseCase,
    private val observeInstalledShaderPacks: ObserveInstalledShaderPacksUseCase,
    private val setShaderPackEnabled: SetShaderPackEnabledUseCase,
    private val removeShaderPack: RemoveShaderPackUseCase,
    private val launchInstance: LaunchInstanceUseCase,
    private val cancelInstance: CancelInstanceUseCase,
    private val cancelInstanceSetup: CancelInstanceSetupUseCase,
    private val stopInstance: StopInstanceUseCase,
    private val killInstance: KillInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val snackbarService: SnackbarService,
    private val updateInstance: UpdateInstanceUseCase,
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val observeAvailableLoaders: ObserveAvailableLoadersUseCase,
    private val observeLoaderVersions: ObserveLoaderVersionsUseCase,
    private val observeJavaInstallations: ObserveJavaInstallationsUseCase,
    private val installJavaRuntime: InstallJavaRuntimeUseCase,
    private val observeJavaRuntimeInstallProgress: ObserveJavaRuntimeInstallProgressUseCase,
    private val observeManualDownloadItems: ObserveManualDownloadItemsUseCase,
    private val updateManualDownloadItems: UpdateManualDownloadItemsUseCase,
    private val awaitManualDownload: AwaitManualDownloadUseCase,
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val hasActiveInstanceJob: HasActiveInstanceJobUseCase,
    private val modpackInstallRunner: ModpackInstallRunner,
    private val login: LoginUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val setInstanceIcon: SetInstanceIconUseCase,
    private val removeInstanceIcon: RemoveInstanceIconUseCase,
    private val getInstance: GetInstanceUseCase,
    private val getProject: GetProjectUseCase,
    private val setInstanceIconFromUrl: SetInstanceIconFromUrlUseCase,
    private val importMod: ImportModUseCase,
    private val importResourcePack: ImportResourcePackUseCase,
    private val importShaderPack: ImportShaderPackUseCase,
    private val openInstanceFolder: OpenInstanceFolderUseCase,
    private val getTotalPhysicalMemory: GetTotalPhysicalMemoryUseCase,
    private val detectNativeLibraryPath: DetectNativeLibraryPathUseCase,
    private val systemService: SystemService,
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
    private val resolveJavaInstallation: ResolveJavaInstallationUseCase,
    private val getRequiredJavaMajorVersion: GetRequiredJavaMajorVersionUseCase,
) : Presenter<InstanceDetailsState, InstanceDetailsEvent> {
    @Composable
    override fun present(): Store<InstanceDetailsState, InstanceDetailsEvent> {
        val instance by observeInstanceView()
        val gameVersionList by observeGameVersionListView()
        val javaInstallations by observeJavaInstallationsView()
        val (promptInstallJava, setPromptInstallJava) = remember { mutableStateOf<Int?>(null) }
        val logs by observeInstanceLogsView()
        val installedMods by observeInstalledModsView()
        val installedResourcePacks by observeInstalledResourcePacksView()
        val installedShaderPacks by observeInstalledShaderPacksView()
        val (promptDeleteConfirmation, setPromptDeleteConfirmation) = rememberSaveable { mutableStateOf(false) }
        val (promptRemoveConfirmation, setPromptRemoveConfirmation) =
            remember { mutableStateOf<PendingRemoval?>(null) }
        val (promptFinishInstallation, setPromptFinishInstallation) = rememberSaveable { mutableStateOf(false) }
        val finishInstallationDialog = remember(screen.instanceId) {
            FinishInstallationDialog(screen.instanceId, observeManualDownloadItems) { url -> copyToClipboard(url) }
        }
        val loginState = remember { mutableStateOf(LoginStateView()) }
        val (promptLogin, setPromptLogin) = remember { mutableStateOf(false) }
        val loginDialog = remember { LoginDialog(loginState) { url -> copyToClipboard(url) } }
        val (loginJob, setLoginJob) = remember { mutableStateOf<Job?>(null) }
        val javaInstallState = remember { mutableStateOf(JavaInstallProgressView()) }
        val (promptJavaInstall, setPromptJavaInstall) = remember { mutableStateOf(false) }
        val installJavaProgressDialog = remember { InstallJavaProgressDialog(javaInstallState) }
        val (javaInstallJob, setJavaInstallJob) = remember { mutableStateOf<Job?>(null) }
        val (selectedTab, setSelectedTab) = rememberSaveable {
            mutableStateOf(screen.initialTab ?: InstanceDetailsTab.Options)
        }
        val (logSearchQuery, setLogSearchQuery) = rememberSaveable { mutableStateOf("") }
        val (logLevelFilter, setLogLevelFilter) = rememberSaveable {
            mutableStateOf(InstanceLogEventView.Level.entries.toSet())
        }
        val configuration by observeConfiguration().collectProgressiveAsState()
        val (editedInstance, setEditedInstance) = rememberSaveable { mutableStateOf(InstanceEditState()) }
        val totalMemoryMb = remember {
            ((getTotalPhysicalMemory() ?: (16384L * 1024 * 1024)) / (1024 * 1024)).toInt()
        }
        val showNativeLibraryOptions = systemService.osType == OsType.Linux
        var detectedGlfwPath by remember { mutableStateOf<String?>(null) }
        var detectedOpenAlPath by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(showNativeLibraryOptions) {
            if (showNativeLibraryOptions) {
                detectedGlfwPath = detectNativeLibraryPath(NativeLibraryType.Glfw)
                detectedOpenAlPath = detectNativeLibraryPath(NativeLibraryType.OpenAl)
            }
        }

        val currentInstance = instance.valueOrNull
        val effectiveGameVersion = editedInstance.gameVersion ?: currentInstance?.gameVersion

        var requiredJavaMajorVersion by remember { mutableStateOf<Int?>(null) }
        var inheritedJavaInstallation by remember { mutableStateOf<JavaInstallation?>(null) }
        LaunchedEffect(effectiveGameVersion) {
            val majorVersion = effectiveGameVersion?.let { getRequiredJavaMajorVersion(it) }
            requiredJavaMajorVersion = majorVersion
            inheritedJavaInstallation = majorVersion?.let { resolveJavaInstallation(it, instanceOverrideId = null) }
        }
        val effectiveLoaderType = when (val edit = editedInstance.modLoaderType) {
            is ModLoaderTypeEdit.Loader -> edit.type
            ModLoaderTypeEdit.Vanilla -> null
            null -> currentInstance?.loaderType
        }

        val loaderList by observeAvailableLoadersView(effectiveGameVersion)
        val loaderVersionList by observeLoaderVersionsView(effectiveGameVersion, effectiveLoaderType)

        LaunchedEffect(editedInstance.modLoaderType, editedInstance.modLoaderVersion, loaderVersionList) {
            val versions = loaderVersionList.valueOrNull
            if (editedInstance.modLoaderType is ModLoaderTypeEdit.Loader &&
                editedInstance.modLoaderVersion == null &&
                versions != null
            ) {
                val recommended = versions.firstOrNull { it.stable } ?: versions.firstOrNull()
                if (recommended != null) {
                    setEditedInstance(editedInstance.copy(modLoaderVersion = recommended.version))
                }
            }
        }

        return InstanceDetailsState(
            instance = instance,
            editedInstance = editedInstance,
            gameVersionList = gameVersionList.valueOrNull ?: emptyList(),
            loaderList = loaderList.valueOrNull ?: emptyList(),
            loaderVersionList = loaderVersionList.valueOrNull ?: emptyList(),
            availableJavaInstallations = javaInstallations.valueOrNull ?: emptyList(),
            promptInstallJava = promptInstallJava,
            totalMemoryMb = totalMemoryMb,
            showNativeLibraryOptions = showNativeLibraryOptions,
            detectedGlfwPath = detectedGlfwPath,
            detectedOpenAlPath = detectedOpenAlPath,
            globalJvmMemoryOptions = configuration.valueOrNull?.jvmMemoryOptions ?: JvmMemoryOptions(),
            globalNativeLibraryOptions = configuration.valueOrNull?.nativeLibraryOptions ?: NativeLibraryOptions(),
            inheritedJavaInstallation = inheritedJavaInstallation,
            requiredJavaMajorVersion = requiredJavaMajorVersion,
            promptDeleteConfirmation = promptDeleteConfirmation,
            selectedTab = selectedTab,
            logs = logs,
            logSearchQuery = logSearchQuery,
            logLevelFilter = logLevelFilter,
            wrapLogLines = configuration.valueOrNull?.wrapLogLines ?: true,
            installedMods = installedMods,
            installedResourcePacks = installedResourcePacks,
            installedShaderPacks = installedShaderPacks,
            promptRemoveConfirmation = promptRemoveConfirmation,
            promptFinishInstallation = promptFinishInstallation,
            finishInstallationDialog = finishInstallationDialog,
            promptLogin = promptLogin,
            loginDialog = loginDialog,
            promptJavaInstall = promptJavaInstall,
            installJavaProgressDialog = installJavaProgressDialog
        ) + { event ->
            handleEvent(
                event = event,
                setPromptDeleteConfirmation = setPromptDeleteConfirmation,
                setSelectedTab = setSelectedTab,
                setEditedInstance = setEditedInstance,
                editedInstance = editedInstance,
                currentInstance = currentInstance,
                promptRemoveConfirmation = promptRemoveConfirmation,
                setPromptRemoveConfirmation = setPromptRemoveConfirmation,
                setPromptFinishInstallation = setPromptFinishInstallation,
                promptInstallJava = promptInstallJava,
                setPromptInstallJava = setPromptInstallJava,
                loginState = loginState,
                setPromptLogin = setPromptLogin,
                loginJob = loginJob,
                setLoginJob = setLoginJob,
                javaInstallState = javaInstallState,
                setPromptJavaInstall = setPromptJavaInstall,
                javaInstallJob = javaInstallJob,
                setJavaInstallJob = setJavaInstallJob,
                logLevelFilter = logLevelFilter,
                setLogSearchQuery = setLogSearchQuery,
                setLogLevelFilter = setLogLevelFilter
            )
        }
    }

    private fun handleEvent(
        event: InstanceDetailsEvent,
        setPromptDeleteConfirmation: (Boolean) -> Unit,
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        setEditedInstance: (InstanceEditState) -> Unit,
        editedInstance: InstanceEditState,
        currentInstance: InstanceView?,
        promptRemoveConfirmation: PendingRemoval?,
        setPromptRemoveConfirmation: (PendingRemoval?) -> Unit,
        setPromptFinishInstallation: (Boolean) -> Unit,
        promptInstallJava: Int?,
        setPromptInstallJava: (Int?) -> Unit,
        loginState: MutableState<LoginStateView>,
        setPromptLogin: (Boolean) -> Unit,
        loginJob: Job?,
        setLoginJob: (Job?) -> Unit,
        javaInstallState: MutableState<JavaInstallProgressView>,
        setPromptJavaInstall: (Boolean) -> Unit,
        javaInstallJob: Job?,
        setJavaInstallJob: (Job?) -> Unit,
        logLevelFilter: Set<InstanceLogEventView.Level>,
        setLogSearchQuery: (String) -> Unit,
        setLogLevelFilter: (Set<InstanceLogEventView.Level>) -> Unit
    ) {
        if (handleLifecycleEvent(
                event,
                setSelectedTab,
                setPromptDeleteConfirmation,
                promptInstallJava,
                setPromptInstallJava,
                loginState,
                setPromptLogin,
                loginJob,
                setLoginJob,
                javaInstallState,
                setPromptJavaInstall,
                javaInstallJob,
                setJavaInstallJob
            )
        ) return
        if (handleEditEvent(event, editedInstance, setEditedInstance, setSelectedTab)) return
        if (handleContentEvent(event, promptRemoveConfirmation, setPromptRemoveConfirmation)) return
        if (handleLogsEvent(event, logLevelFilter, setLogSearchQuery, setLogLevelFilter)) return
        handleNavigationAndMiscEvent(event, currentInstance, setPromptFinishInstallation)
    }

    /** Search/filter/wrap-lines events for the Logs tab. Returns whether [event] was one of these. */
    private fun handleLogsEvent(
        event: InstanceDetailsEvent,
        logLevelFilter: Set<InstanceLogEventView.Level>,
        setLogSearchQuery: (String) -> Unit,
        setLogLevelFilter: (Set<InstanceLogEventView.Level>) -> Unit
    ): Boolean {
        when (event) {
            is InstanceDetailsEvent.OnLogSearchQueryChange -> setLogSearchQuery(event.query)
            is InstanceDetailsEvent.OnLogLevelFilterToggle -> setLogLevelFilter(
                logLevelFilter.toggle(event.level).toSet()
            )

            InstanceDetailsEvent.OnWrapLogLinesToggle -> globalScope.launch {
                updateConfiguration { copy(wrapLogLines = !wrapLogLines) }
            }

            else -> return false
        }
        return true
    }

    /** Launch/stop/delete and other whole-instance lifecycle events. Returns whether [event] was one of these. */
    private fun handleLifecycleEvent(
        event: InstanceDetailsEvent,
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        setPromptDeleteConfirmation: (Boolean) -> Unit,
        promptInstallJava: Int?,
        setPromptInstallJava: (Int?) -> Unit,
        loginState: MutableState<LoginStateView>,
        setPromptLogin: (Boolean) -> Unit,
        loginJob: Job?,
        setLoginJob: (Job?) -> Unit,
        javaInstallState: MutableState<JavaInstallProgressView>,
        setPromptJavaInstall: (Boolean) -> Unit,
        javaInstallJob: Job?,
        setJavaInstallJob: (Job?) -> Unit
    ): Boolean {
        when (event) {
            InstanceDetailsEvent.Launch -> handleLaunch(
                setSelectedTab, setPromptInstallJava, loginState, setPromptLogin, setLoginJob
            )

            InstanceDetailsEvent.OnInstallJavaConfirm -> handleInstallJavaConfirm(
                promptInstallJava,
                setPromptInstallJava,
                javaInstallState,
                setPromptJavaInstall,
                setJavaInstallJob
            )

            InstanceDetailsEvent.OnInstallJavaDismiss -> setPromptInstallJava(null)
            InstanceDetailsEvent.OnJavaInstallDialogDismiss -> handleJavaInstallDialogDismiss(
                javaInstallJob,
                javaInstallState,
                setPromptJavaInstall,
                setSelectedTab,
                setPromptInstallJava,
                loginState,
                setPromptLogin,
                setLoginJob
            )

            InstanceDetailsEvent.CancelLogin -> handleCancelLogin(
                loginJob,
                loginState,
                setPromptLogin,
                setLoginJob,
                setSelectedTab,
                setPromptInstallJava
            )

            InstanceDetailsEvent.Delete -> setPromptDeleteConfirmation(true)
            is InstanceDetailsEvent.ConfirmDelete -> handleConfirmDelete(event, setPromptDeleteConfirmation)
            InstanceDetailsEvent.Cancel -> cancelInstance(screen.instanceId)
            InstanceDetailsEvent.CancelSetup -> cancelInstanceSetup(screen.instanceId)
            InstanceDetailsEvent.Stop -> globalScope.launch { stopInstance(screen.instanceId) }
            InstanceDetailsEvent.Kill -> globalScope.launch { killInstance(screen.instanceId) }
            else -> return false
        }
        return true
    }

    /** Tab selection and the name/version/loader edit-and-save flow. Returns whether [event] was one of these. */
    private fun handleEditEvent(
        event: InstanceDetailsEvent,
        editedInstance: InstanceEditState,
        setEditedInstance: (InstanceEditState) -> Unit,
        setSelectedTab: (InstanceDetailsTab) -> Unit
    ): Boolean {
        when (event) {
            is InstanceDetailsEvent.SelectTab -> setSelectedTab(event.tab)
            is InstanceDetailsEvent.UpdateInstance -> setEditedInstance(event.update(editedInstance))
            is InstanceDetailsEvent.OnLoaderChange -> handleLoaderChange(event, editedInstance, setEditedInstance)
            is InstanceDetailsEvent.OnLoaderVersionChange ->
                setEditedInstance(editedInstance.copy(modLoaderVersion = event.version))

            is InstanceDetailsEvent.OnJavaOverrideChange -> setEditedInstance(
                editedInstance.copy(
                    javaOverride = event.installationId?.let(JavaOverrideEdit::Specific) ?: JavaOverrideEdit.UseDefault
                )
            )

            InstanceDetailsEvent.CancelEdit -> setEditedInstance(InstanceEditState())
            InstanceDetailsEvent.SaveInstance -> handleSaveInstance(editedInstance, setEditedInstance)
            else -> return false
        }
        return true
    }

    /** Enable/disable and removal of installed mods/resource packs/shader packs. Returns whether [event] was one of these. */
    private fun handleContentEvent(
        event: InstanceDetailsEvent,
        promptRemoveConfirmation: PendingRemoval?,
        setPromptRemoveConfirmation: (PendingRemoval?) -> Unit
    ): Boolean {
        when (event) {
            is InstanceDetailsEvent.OnModEnabledChange -> globalScope.launch {
                setModEnabled(screen.instanceId, event.fileName, event.enabled)
            }

            is InstanceDetailsEvent.OnResourcePackEnabledChange -> globalScope.launch {
                setResourcePackEnabled(screen.instanceId, event.fileName, event.enabled)
            }

            is InstanceDetailsEvent.OnShaderPackEnabledChange -> globalScope.launch {
                setShaderPackEnabled(screen.instanceId, event.fileName, event.enabled)
            }

            is InstanceDetailsEvent.OnRemoveClick ->
                setPromptRemoveConfirmation(PendingRemoval(event.kind, event.fileName, event.title))

            is InstanceDetailsEvent.OnConfirmRemove ->
                handleConfirmRemove(event, promptRemoveConfirmation, setPromptRemoveConfirmation)

            else -> return false
        }
        return true
    }

    /** Navigation, custom icon, and manual-download-installation events. */
    private fun handleNavigationAndMiscEvent(
        event: InstanceDetailsEvent,
        currentInstance: InstanceView?,
        setPromptFinishInstallation: (Boolean) -> Unit
    ) {
        when (event) {
            is InstanceDetailsEvent.OnOpenProjectClick -> navigator.goTo(
                ProjectDetailsScreen(event.projectId from event.platformId, preferredInstanceId = screen.instanceId)
            )

            is InstanceDetailsEvent.OnAddContentClick -> navigator.goTo(
                ProjectListScreen(
                    initialType = event.type,
                    initialGameVersion = currentInstance?.gameVersion,
                    initialLoader = currentInstance?.loaderType,
                    sourceInstanceId = screen.instanceId
                )
            )

            is InstanceDetailsEvent.OnIconSelected -> globalScope.launch {
                either {
                    setInstanceIcon(
                        screen.instanceId,
                        event.path
                    )
                }.onLeft { error -> snackbarService.error { error.message } }
            }

            InstanceDetailsEvent.OnRemoveIconClick -> globalScope.launch {
                either { removeInstanceIcon(screen.instanceId) }.onLeft { error -> snackbarService.error { error.message } }
            }

            InstanceDetailsEvent.OnRestoreIconClick -> globalScope.launch {
                recover({
                    val instance = getInstance(screen.instanceId)
                    val modpackInfo = instance.modpackInfo
                    if (modpackInfo != null) {
                        recover({
                            val logoUrl = getProject(modpackInfo.projectId from modpackInfo.platformId).logoUrl
                            if (logoUrl == null || !setInstanceIconFromUrl(instance, logoUrl)) {
                                snackbarService.error { stringResource(InstanceRes.string.errorRestoreIconFailed) }
                            }
                        }) { _: ProjectDetailsError ->
                            snackbarService.error { stringResource(InstanceRes.string.errorRestoreIconFailed) }
                        }
                    }
                }) { _: InstanceError ->
                    snackbarService.error { stringResource(InstanceRes.string.errorRestoreIconFailed) }
                }
            }

            is InstanceDetailsEvent.OnImportContentClick -> globalScope.launch {
                val imported = when (event.kind) {
                    RemovalKind.Mod -> importMod(screen.instanceId, event.path)
                    RemovalKind.ResourcePack -> importResourcePack(screen.instanceId, event.path)
                    RemovalKind.ShaderPack -> importShaderPack(screen.instanceId, event.path)
                }
                if (!imported) snackbarService.error { stringResource(InstanceRes.string.errorImportFailed) }
            }

            is InstanceDetailsEvent.OnOpenInstanceFolderClick -> globalScope.launch {
                val opened = openInstanceFolder(screen.instanceId, event.folder)
                if (!opened) snackbarService.error { stringResource(InstanceRes.string.errorOpenFolderFailed) }
            }

            InstanceDetailsEvent.OnFinishInstallationClick -> {
                setPromptFinishInstallation(true)
                currentInstance?.let(::resumeManualDownloadsIfNeeded)
            }

            InstanceDetailsEvent.OnFinishInstallationDismiss -> setPromptFinishInstallation(false)
            InstanceDetailsEvent.OnResumeInstallationClick -> currentInstance?.let(::resumeInstallationIfNeeded)
            else -> Unit
        }
    }

    private fun resumeInstallationIfNeeded(instance: InstanceView) {
        if (instance.status !is InstanceStatus.Installing.Interrupted) return
        val modpackInfo = instance.modpackInfo ?: return
        if (hasActiveInstanceJob(screen.instanceId)) return

        val instanceId = screen.instanceId
        val job = globalScope.launch {
            val ok = modpackInstallRunner.run(instanceId, modpackInfo, initialManifest = null)
            updateInstanceStatus(instanceId, if (ok) InstanceStatus.Idle else InstanceStatus.Failed.InstallFailed)
            notifyModpackInstallResult(instance.name, ok)
        }
        updateInstanceJob(instanceId, job)
        job.invokeOnCompletion { updateInstanceJob(instanceId, null) }
    }

    private fun resumeManualDownloadsIfNeeded(instance: InstanceView) {
        if (instance.status !is InstanceStatus.Installing.AwaitingManualDownloads) return
        if (instance.pendingManualDownloads.isEmpty()) return
        if (hasActiveInstanceJob(screen.instanceId)) return

        val instanceId = screen.instanceId
        val candidates = instance.pendingManualDownloads.map { it.toManualDownloadCandidate(instanceId) }
        val job = globalScope.launch {
            val ok = runManualDownloads(
                candidates, awaitManualDownload, recordInstalledContentOrigin, snackbarService
            ) { items ->
                updateManualDownloadItems(instanceId, items)
                persistPendingManualDownloads(instanceId, candidates, items, updateInstance)
            }
            updateInstanceStatus(instanceId, if (ok) InstanceStatus.Idle else InstanceStatus.Failed.InstallFailed)
        }
        updateInstanceJob(instanceId, job)
        job.invokeOnCompletion { updateInstanceJob(instanceId, null) }
    }

    private fun handleLaunch(
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        setPromptInstallJava: (Int?) -> Unit,
        loginState: MutableState<LoginStateView>,
        setPromptLogin: (Boolean) -> Unit,
        setLoginJob: (Job?) -> Unit,
    ) = globalScope.launch {
        setSelectedTab(InstanceDetailsTab.Logs)
        either { launchInstance(screen.instanceId) }.onLeft { error ->
            when (error) {
                is InstanceLaunchError.NoCompatibleJava -> setPromptInstallJava(error.majorVersion)
                InstanceLaunchError.LoginRequired -> startLogin(loginState, setPromptLogin, setLoginJob)
                else -> snackbarService.error { error.message }
            }
        }
    }

    private fun startLogin(
        loginState: MutableState<LoginStateView>,
        setPromptLogin: (Boolean) -> Unit,
        setLoginJob: (Job?) -> Unit,
    ) {
        loginState.value = LoginStateView()
        setPromptLogin(true)
        val job = globalScope.launch { collectLoginState(loginState) }
        job.invokeOnCompletion { if (it != null && it !is CancellationException) onLoginFailure(it, loginState) }
        setLoginJob(job)
    }

    private suspend fun collectLoginState(state: MutableState<LoginStateView>) = login().collect {
        when (it) {
            is LoginState.RequireLogin -> state.update { copy(isLoggingIn = true, url = it.url) }
            is LoginState.Progress -> state.update { copy(isLoggingIn = true, url = null, progress = it.step.progress) }
            is LoginState.Success -> state.update {
                copy(
                    isLoggingIn = false,
                    url = null,
                    progress = null,
                    success = true
                )
            }

            is LoginState.Error -> state.update {
                copy(
                    isLoggingIn = false,
                    url = null,
                    progress = null,
                    error = it.error
                )
            }
        }
    }

    private fun onLoginFailure(error: Throwable, state: MutableState<LoginStateView>) {
        state.update { copy(isLoggingIn = false, url = null, progress = null, error = error) }
    }

    private fun handleCancelLogin(
        loginJob: Job?,
        loginState: MutableState<LoginStateView>,
        setPromptLogin: (Boolean) -> Unit,
        setLoginJob: (Job?) -> Unit,
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        setPromptInstallJava: (Int?) -> Unit,
    ) {
        loginJob?.cancel(CancellationException("Login cancelled by user"))
        setPromptLogin(false)
        if (loginState.value.success) {
            handleLaunch(setSelectedTab, setPromptInstallJava, loginState, setPromptLogin, setLoginJob)
        }
    }

    private fun handleInstallJavaConfirm(
        promptInstallJava: Int?,
        setPromptInstallJava: (Int?) -> Unit,
        javaInstallState: MutableState<JavaInstallProgressView>,
        setPromptJavaInstall: (Boolean) -> Unit,
        setJavaInstallJob: (Job?) -> Unit,
    ) {
        setPromptInstallJava(null)
        val majorVersion = promptInstallJava ?: return
        javaInstallState.value = JavaInstallProgressView(majorVersion = majorVersion)
        setPromptJavaInstall(true)
        val job = globalScope.launch {
            coroutineScope {
                val progressJob = launch {
                    observeJavaRuntimeInstallProgress().collect { byVersion ->
                        val status = byVersion[majorVersion]
                        if (status is JavaRuntimeInstallStatus.Downloading) {
                            javaInstallState.update { copy(progress = status.fraction) }
                        }
                    }
                }
                either { installJavaRuntime(majorVersion) }
                    .onLeft { installError ->
                        javaInstallState.update {
                            copy(done = true, error = {
                                stringResource(
                                    InstanceRes.string.javaInstallFailed,
                                    majorVersion,
                                    installError.toString()
                                )
                            })
                        }
                    }
                    .onRight { javaInstallState.update { copy(done = true) } }
                progressJob.cancel()
            }
        }
        setJavaInstallJob(job)
    }

    private fun handleJavaInstallDialogDismiss(
        javaInstallJob: Job?,
        javaInstallState: MutableState<JavaInstallProgressView>,
        setPromptJavaInstall: (Boolean) -> Unit,
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        setPromptInstallJava: (Int?) -> Unit,
        loginState: MutableState<LoginStateView>,
        setPromptLogin: (Boolean) -> Unit,
        setLoginJob: (Job?) -> Unit,
    ) {
        javaInstallJob?.cancel()
        setPromptJavaInstall(false)
        val succeeded = javaInstallState.value.done && javaInstallState.value.error == null
        if (succeeded) {
            handleLaunch(setSelectedTab, setPromptInstallJava, loginState, setPromptLogin, setLoginJob)
        }
    }

    private fun handleConfirmDelete(
        event: InstanceDetailsEvent.ConfirmDelete,
        setPromptDeleteConfirmation: (Boolean) -> Unit
    ) {
        setPromptDeleteConfirmation(false)
        if (!event.result) return
        globalScope.launch {
            either { deleteInstance(screen.instanceId) }
                .onLeft { error -> snackbarService.error { error.message } }
                .onRight {
                    navigator.pop()
                    navigator.goTo(InstanceListScreen)
                }
        }
    }

    private fun handleLoaderChange(
        event: InstanceDetailsEvent.OnLoaderChange,
        editedInstance: InstanceEditState,
        setEditedInstance: (InstanceEditState) -> Unit
    ) = setEditedInstance(
        editedInstance.copy(
            modLoaderType = event.loader?.let(ModLoaderTypeEdit::Loader) ?: ModLoaderTypeEdit.Vanilla,
            // Reset so the recommended-version effect can pick one that's valid for the new type.
            modLoaderVersion = null
        )
    )

    private fun handleConfirmRemove(
        event: InstanceDetailsEvent.OnConfirmRemove,
        promptRemoveConfirmation: PendingRemoval?,
        setPromptRemoveConfirmation: (PendingRemoval?) -> Unit
    ) {
        setPromptRemoveConfirmation(null)
        if (!event.result || promptRemoveConfirmation == null) return
        globalScope.launch {
            when (promptRemoveConfirmation.kind) {
                RemovalKind.Mod -> removeMod(screen.instanceId, promptRemoveConfirmation.fileName)
                RemovalKind.ResourcePack -> removeResourcePack(screen.instanceId, promptRemoveConfirmation.fileName)
                RemovalKind.ShaderPack -> removeShaderPack(screen.instanceId, promptRemoveConfirmation.fileName)
            }
        }
    }

    private fun handleSaveInstance(editedInstance: InstanceEditState, setEditedInstance: (InstanceEditState) -> Unit) =
        globalScope.launch {
            either {
                updateInstance(screen.instanceId) { it.withEdits(editedInstance).copy(updatedAt = Clock.System.now()) }
                setEditedInstance(InstanceEditState())
            }.onLeft { error -> snackbarService.error { error.toString() } }
        }

    private fun Instance.withEdits(editedInstance: InstanceEditState): Instance {
        var updated = editedInstance.name?.let { name -> copy(name = name) } ?: this
        updated = editedInstance.gameVersion?.let { version -> updated.copy(gameVersion = version) } ?: updated

        if (editedInstance.modLoaderType != null || editedInstance.modLoaderVersion != null) {
            val type = when (val edit = editedInstance.modLoaderType) {
                is ModLoaderTypeEdit.Loader -> edit.type
                ModLoaderTypeEdit.Vanilla -> null
                null -> updated.modLoader?.type
            }
            updated = updated.copy(
                modLoader = type?.let { loaderType ->
                    InstanceModLoader(
                        type = loaderType,
                        version = editedInstance.modLoaderVersion ?: updated.modLoader?.version.orEmpty()
                    )
                }
            )
        }

        updated = editedInstance.javaOverride?.let { override ->
            updated.copy(javaInstallationId = (override as? JavaOverrideEdit.Specific)?.installationId)
        } ?: updated

        updated = editedInstance.jvmMemoryOptionsEdit?.let { edit ->
            updated.copy(jvmMemoryOptions = edit)
        } ?: updated

        updated = editedInstance.nativeLibraryOptionsEdit?.let { edit ->
            updated.copy(nativeLibraryOptions = edit)
        } ?: updated

        return updated
    }


    @Composable
    private fun observeInstanceView(): State<ProgressiveOutcome<InstanceError, InstanceView>> =
        produceAndCollectAsProgressiveState(screen.instanceId) {
            observeInstance(screen.instanceId).map { outcome -> outcome.map { it.toView() } }
        }

    @Composable
    private fun observeJavaInstallationsView(): State<ProgressiveOutcome<Nothing, List<JavaInstallation>>> =
        produceAndCollectAsProgressiveState { observeJavaInstallations() }

    @Composable
    private fun observeInstanceLogsView(): State<List<InstanceLogEventView>?> =
        produceAndCollectAsRetainedState(observeInstanceLogs, screen, initial = null) {
            observeInstanceLogs(screen.instanceId).map { list -> list?.map { it.toView() } }
        }

    @Composable
    private fun observeInstalledModsView(): State<List<InstalledContentFileView>?> =
        produceAndCollectAsRetainedState(observeInstalledMods, screen, initial = null) {
            observeInstalledMods(screen.instanceId).map { outcome -> outcome.valueOrNull?.map { it.toView() } }
        }

    @Composable
    private fun observeInstalledResourcePacksView(): State<List<InstalledContentFileView>?> =
        produceAndCollectAsRetainedState(observeInstalledResourcePacks, screen, initial = null) {
            observeInstalledResourcePacks(screen.instanceId).map { outcome -> outcome.valueOrNull?.map { it.toView() } }
        }

    @Composable
    private fun observeInstalledShaderPacksView(): State<List<InstalledContentFileView>?> =
        produceAndCollectAsRetainedState(observeInstalledShaderPacks, screen, initial = null) {
            observeInstalledShaderPacks(screen.instanceId).map { outcome -> outcome.valueOrNull?.map { it.toView() } }
        }

    @Composable
    private fun observeGameVersionListView(): State<ProgressiveOutcome<GameVersionError.InvalidManifest, List<GameVersionView>>> =
        produceAndCollectAsProgressiveState {
            observeGameVersionList().map { res -> res.map { it.toView() } }
        }

    @Composable
    private fun observeAvailableLoadersView(gameVersion: String?): State<ProgressiveOutcome<LoaderError, List<ModLoaderType>>> =
        produceAndCollectAsProgressiveState(gameVersion) {
            gameVersion?.let { observeAvailableLoaders(it) } ?: flowOf(ProgressiveOutcome.Incomplete())
        }

    @Composable
    private fun observeLoaderVersionsView(
        gameVersion: String?,
        loader: ModLoaderType?
    ): State<ProgressiveOutcome<LoaderError, List<LoaderVersion>>> =
        produceAndCollectAsProgressiveState(gameVersion, loader) {
            if (gameVersion != null && loader != null) observeLoaderVersions(gameVersion, loader)
            else flowOf(ProgressiveOutcome.Incomplete())
        }

    @CircuitInject(InstanceDetailsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: InstanceDetailsScreen, navigator: Navigator): InstanceDetailsPresenter
    }
}
