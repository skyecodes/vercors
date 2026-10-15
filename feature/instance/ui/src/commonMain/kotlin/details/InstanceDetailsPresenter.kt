package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.core.ui.state.update
import app.vercors.feature.account.domain.usecase.LoginUseCase
import app.vercors.feature.account.ui.list.LoginDialog
import app.vercors.feature.account.ui.list.LoginStateView
import app.vercors.feature.account.ui.list.startLoginFlow
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceIconError
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.copy.CopyInstanceDialog
import app.vercors.feature.instance.ui.copy.CopyInstanceResult
import app.vercors.feature.instance.ui.copy.CopyInstanceTarget
import app.vercors.feature.instance.ui.details.content.InstalledContentPresenter
import app.vercors.feature.instance.ui.details.logs.InstanceLogsPresenter
import app.vercors.feature.instance.ui.details.options.InstanceOptionsPresenter
import app.vercors.feature.instance.ui.details.servers.InstanceServersPresenter
import app.vercors.feature.instance.ui.details.worlds.InstalledWorldsPresenter
import app.vercors.feature.instance.ui.export.ExportInstanceDialog
import app.vercors.feature.instance.ui.export.ExportInstanceTarget
import app.vercors.feature.instance.ui.install.FinishInstallationDialog
import app.vercors.feature.instance.ui.install.LocalPackUpdateLauncher
import app.vercors.feature.instance.ui.install.notifyModpackInstallResult
import app.vercors.feature.instance.ui.list.InstanceListScreen
import app.vercors.feature.instance.ui.options.copy.CopyOptionsDialog
import app.vercors.feature.instance.ui.options.copy.CopyOptionsTarget
import app.vercors.feature.instance.ui.options.savedefault.SaveDefaultOptionsDialog
import app.vercors.feature.instance.ui.options.savedefault.SaveDefaultOptionsTarget
import app.vercors.feature.java.domain.model.JavaRuntimeInstallStatus
import app.vercors.feature.java.domain.usecase.InstallJavaRuntimeUseCase
import app.vercors.feature.java.domain.usecase.ObserveJavaRuntimeInstallProgressUseCase
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.model.QuickPlayTarget
import app.vercors.feature.launcher.domain.usecase.CancelInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.KillInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.StopInstanceUseCase
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.from
import app.vercors.feature.project.domain.model.toManualDownloadCandidate
import app.vercors.feature.project.domain.model.update.ModpackUpdate
import app.vercors.feature.project.domain.usecase.*
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
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
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

private val logger = KotlinLogging.logger {}

@AssistedInject
class InstanceDetailsPresenter(
    @Assisted private val screen: InstanceDetailsScreen,
    @Assisted private val navigator: Navigator,
    private val globalScope: CoroutineScope,
    private val observeInstance: ObserveInstanceUseCase,
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val observeInstanceLogs: ObserveInstanceLogsUseCase,
    private val installedContentPresenterFactory: InstalledContentPresenter.Factory,
    private val installedWorldsPresenterFactory: InstalledWorldsPresenter.Factory,
    private val instanceServersPresenterFactory: InstanceServersPresenter.Factory,
    private val instanceLogsPresenterFactory: InstanceLogsPresenter.Factory,
    private val instanceOptionsPresenterFactory: InstanceOptionsPresenter.Factory,
    private val launchInstance: LaunchInstanceUseCase,
    private val cancelInstance: CancelInstanceUseCase,
    private val cancelInstanceSetup: CancelInstanceSetupUseCase,
    private val stopInstance: StopInstanceUseCase,
    private val killInstance: KillInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val snackbarService: SnackbarService,
    private val installJavaRuntime: InstallJavaRuntimeUseCase,
    private val observeJavaRuntimeInstallProgress: ObserveJavaRuntimeInstallProgressUseCase,
    private val observeManualDownloadItems: ObserveManualDownloadItemsUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val hasActiveInstanceJob: HasActiveInstanceJobUseCase,
    private val runModpackInstall: RunModpackInstallUseCase,
    private val runManualDownloads: RunManualDownloadsUseCase,
    private val trackPendingManualDownloads: TrackPendingManualDownloadsUseCase,
    private val login: LoginUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val setInstanceIcon: SetInstanceIconUseCase,
    private val removeInstanceIcon: RemoveInstanceIconUseCase,
    private val getInstance: GetInstanceUseCase,
    private val getProject: GetProjectUseCase,
    private val setInstanceIconFromUrl: SetInstanceIconFromUrlUseCase,
    private val openInstanceFolder: OpenInstanceFolderUseCase,
    private val copyInstanceDialogFactory: CopyInstanceDialog.Factory,
    private val exportInstanceDialogFactory: ExportInstanceDialog.Factory,
    private val copyOptionsDialogFactory: CopyOptionsDialog.Factory,
    private val saveDefaultOptionsDialogFactory: SaveDefaultOptionsDialog.Factory,
    private val observeInstanceUpdates: ObserveInstanceUpdatesUseCase,
    private val checkInstanceUpdates: CheckInstanceUpdatesUseCase,
    private val shouldAutoCheckInstanceUpdates: ShouldAutoCheckInstanceUpdatesUseCase,
    private val updateModpack: UpdateModpackUseCase,
    private val parseLocalPack: ParseLocalPackUseCase,
    private val getInstalledPack: GetInstalledPackUseCase,
    private val localPackUpdateLauncher: LocalPackUpdateLauncher,
) : Presenter<InstanceDetailsState, InstanceDetailsEvent> {
    @Composable
    override fun present(): Store<InstanceDetailsState, InstanceDetailsEvent> {
        val instance by observeInstanceView()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val horizontalTabs = configuration.valueOrNull?.horizontalTabs == true
        val (promptInstallJava, setPromptInstallJava) = remember { mutableStateOf<Int?>(null) }

        val optionsContent = remember(screen.instanceId) {
            instanceOptionsPresenterFactory.create(screen.instanceId)
        }.present()
        val modsContent = remember(screen.instanceId) {
            installedContentPresenterFactory.create(screen.instanceId, ContentKind.Mod)
        }.present()
        val resourcePacksContent = remember(screen.instanceId) {
            installedContentPresenterFactory.create(screen.instanceId, ContentKind.ResourcePack)
        }.present()
        val shaderPacksContent = remember(screen.instanceId) {
            installedContentPresenterFactory.create(screen.instanceId, ContentKind.ShaderPack)
        }.present()
        val worldsContent = remember(screen.instanceId) {
            installedWorldsPresenterFactory.create(screen.instanceId)
        }.present()
        val serversContent = remember(screen.instanceId) {
            instanceServersPresenterFactory.create(screen.instanceId)
        }.present()
        val logsContent = remember(screen.instanceId) {
            instanceLogsPresenterFactory.create(screen.instanceId)
        }.present()

        val (promptDeleteConfirmation, setPromptDeleteConfirmation) = remember { mutableStateOf(false) }
        val (copyInstanceDialog, setCopyInstanceDialog) = remember { mutableStateOf<CopyInstanceDialog?>(null) }
        val (exportInstanceDialog, setExportInstanceDialog) = remember { mutableStateOf<ExportInstanceDialog?>(null) }
        val (copyOptionsDialog, setCopyOptionsDialog) = remember { mutableStateOf<CopyOptionsDialog?>(null) }
        val (saveDefaultOptionsDialog, setSaveDefaultOptionsDialog) =
            remember { mutableStateOf<SaveDefaultOptionsDialog?>(null) }
        val (promptFinishInstallation, setPromptFinishInstallation) = remember { mutableStateOf(false) }
        val manualDownloadItems = produceAndCollectAsRetainedState(screen.instanceId, initial = emptyList()) {
            observeManualDownloadItems(screen.instanceId)
        }
        val finishInstallationDialog = remember(screen.instanceId) {
            FinishInstallationDialog(manualDownloadItems) { url -> copyToClipboard(url) }
        }
        val loginState = remember { mutableStateOf(LoginStateView()) }
        val (promptLogin, setPromptLogin) = remember { mutableStateOf(false) }
        val loginDialog = remember { LoginDialog(loginState) { url -> copyToClipboard(url) } }
        val (loginJob, setLoginJob) = remember { mutableStateOf<Job?>(null) }
        val javaInstallState = remember { mutableStateOf(JavaInstallProgressView()) }
        val (promptJavaInstallProgress, setPromptJavaInstallProgress) = remember { mutableStateOf(false) }
        val installJavaProgressDialog = remember { InstallJavaProgressDialog(javaInstallState) }
        val (javaInstallJob, setJavaInstallJob) = remember { mutableStateOf<Job?>(null) }
        val (pendingQuickPlayTarget, setPendingQuickPlayTarget) = remember {
            mutableStateOf<QuickPlayTarget?>(null)
        }
        val (selectedTab, setSelectedTab) = rememberSaveable {
            mutableStateOf(screen.initialTab ?: InstanceDetailsTab.Options)
        }
        val currentInstance = instance.valueOrNull
        val modpackUpdate by remember(screen.instanceId) {
            observeInstanceUpdates(screen.instanceId).map { it?.modpack }
        }.collectAsState(null)
        val (promptModpackUpdate, setPromptModpackUpdate) = remember { mutableStateOf(false) }
        val (localPackUpdatePrompt, setLocalPackUpdatePrompt) = remember {
            mutableStateOf<LocalPackUpdatePrompt?>(null)
        }
        AutoCheckUpdatesEffect(currentInstance, configuration.valueOrNull?.checkForInstanceUpdates)

        LaunchedEffect(screen.instanceId, screen.autoLaunch) {
            if (screen.autoLaunch) {
                handleLaunch(
                    setSelectedTab, setPromptInstallJava, loginState, setPromptLogin, setLoginJob,
                    setPendingQuickPlayTarget
                )
            }
        }

        return InstanceDetailsState(
            instance = instance,
            promptInstallJava = promptInstallJava,
            promptDeleteConfirmation = promptDeleteConfirmation,
            copyInstanceDialog = copyInstanceDialog,
            exportInstanceDialog = exportInstanceDialog,
            copyOptionsDialog = copyOptionsDialog,
            saveDefaultOptionsDialog = saveDefaultOptionsDialog,
            selectedTab = selectedTab,
            horizontalTabs = horizontalTabs,
            optionsContent = optionsContent,
            modsContent = modsContent,
            resourcePacksContent = resourcePacksContent,
            shaderPacksContent = shaderPacksContent,
            worldsContent = worldsContent,
            serversContent = serversContent,
            logsContent = logsContent,
            promptFinishInstallation = promptFinishInstallation,
            finishInstallationDialog = finishInstallationDialog,
            promptLogin = promptLogin,
            loginDialog = loginDialog,
            promptJavaInstallProgress = promptJavaInstallProgress,
            installJavaProgressDialog = installJavaProgressDialog,
            modpackUpdate = currentInstance?.let { modpackUpdate?.toView(it.gameVersion) },
            promptModpackUpdate = promptModpackUpdate,
            localPackUpdatePrompt = localPackUpdatePrompt,
        ) + { event ->
            handleEvent(
                event = event,
                setPromptDeleteConfirmation = setPromptDeleteConfirmation,
                setCopyInstanceDialog = setCopyInstanceDialog,
                setExportInstanceDialog = setExportInstanceDialog,
                setCopyOptionsDialog = setCopyOptionsDialog,
                setSaveDefaultOptionsDialog = setSaveDefaultOptionsDialog,
                setSelectedTab = setSelectedTab,
                currentInstance = currentInstance,
                setPromptFinishInstallation = setPromptFinishInstallation,
                promptInstallJava = promptInstallJava,
                setPromptInstallJava = setPromptInstallJava,
                loginState = loginState,
                setPromptLogin = setPromptLogin,
                loginJob = loginJob,
                setLoginJob = setLoginJob,
                javaInstallState = javaInstallState,
                setPromptJavaInstallProgress = setPromptJavaInstallProgress,
                javaInstallJob = javaInstallJob,
                setJavaInstallJob = setJavaInstallJob,
                pendingQuickPlayTarget = pendingQuickPlayTarget,
                setPendingQuickPlayTarget = setPendingQuickPlayTarget,
                modpackUpdate = modpackUpdate,
                setPromptModpackUpdate = setPromptModpackUpdate,
                localPackUpdatePrompt = localPackUpdatePrompt,
                setLocalPackUpdatePrompt = setLocalPackUpdatePrompt,
            )
        }
    }

    // Automatic checks fail silently: they run on every open and a snackbar each time would be noise
    @Composable
    private fun AutoCheckUpdatesEffect(instance: InstanceView?, globalCheckForUpdates: Boolean?) {
        val ready = instance != null && globalCheckForUpdates != null
        LaunchedEffect(screen.instanceId, ready) {
            if (instance == null || globalCheckForUpdates == null) return@LaunchedEffect
            if (instance.status is InstanceStatus.Installing) return@LaunchedEffect
            if (!shouldAutoCheckInstanceUpdates(screen.instanceId, instance.checkForUpdates, globalCheckForUpdates)) {
                return@LaunchedEffect
            }
            globalScope.launch {
                either { checkInstanceUpdates(screen.instanceId, identifyUnknown = false) }
                    .onLeft { error -> logger.warn { "Automatic update check of ${screen.instanceId} failed: $error" } }
            }
        }
    }

    private fun handleEvent(
        event: InstanceDetailsEvent,
        setPromptDeleteConfirmation: (Boolean) -> Unit,
        setCopyInstanceDialog: (CopyInstanceDialog?) -> Unit,
        setExportInstanceDialog: (ExportInstanceDialog?) -> Unit,
        setCopyOptionsDialog: (CopyOptionsDialog?) -> Unit,
        setSaveDefaultOptionsDialog: (SaveDefaultOptionsDialog?) -> Unit,
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        currentInstance: InstanceView?,
        setPromptFinishInstallation: (Boolean) -> Unit,
        promptInstallJava: Int?,
        setPromptInstallJava: (Int?) -> Unit,
        loginState: MutableState<LoginStateView>,
        setPromptLogin: (Boolean) -> Unit,
        loginJob: Job?,
        setLoginJob: (Job?) -> Unit,
        javaInstallState: MutableState<JavaInstallProgressView>,
        setPromptJavaInstallProgress: (Boolean) -> Unit,
        javaInstallJob: Job?,
        setJavaInstallJob: (Job?) -> Unit,
        pendingQuickPlayTarget: QuickPlayTarget?,
        setPendingQuickPlayTarget: (QuickPlayTarget?) -> Unit,
        modpackUpdate: ModpackUpdate?,
        setPromptModpackUpdate: (Boolean) -> Unit,
        localPackUpdatePrompt: LocalPackUpdatePrompt?,
        setLocalPackUpdatePrompt: (LocalPackUpdatePrompt?) -> Unit,
    ) {
        when (event) {
            InstanceDetailsEvent.OnLaunchClick -> handleLaunch(
                setSelectedTab, setPromptInstallJava, loginState, setPromptLogin, setLoginJob,
                setPendingQuickPlayTarget
            )

            is InstanceDetailsEvent.OnQuickJoinClick -> handleLaunch(
                setSelectedTab, setPromptInstallJava, loginState, setPromptLogin, setLoginJob,
                setPendingQuickPlayTarget, event.target
            )

            InstanceDetailsEvent.OnInstallJavaConfirm -> handleInstallJavaConfirm(
                promptInstallJava,
                setPromptInstallJava,
                javaInstallState,
                setPromptJavaInstallProgress,
                setJavaInstallJob
            )

            InstanceDetailsEvent.OnInstallJavaDismiss -> setPromptInstallJava(null)
            InstanceDetailsEvent.OnJavaInstallProgressDismiss -> handleJavaInstallDialogDismiss(
                javaInstallJob,
                javaInstallState,
                setPromptJavaInstallProgress,
                setSelectedTab,
                setPromptInstallJava,
                loginState,
                setPromptLogin,
                setLoginJob,
                setPendingQuickPlayTarget,
                pendingQuickPlayTarget
            )

            InstanceDetailsEvent.OnLoginDismiss -> handleCancelLogin(
                loginJob,
                loginState,
                setPromptLogin,
                setLoginJob,
                setSelectedTab,
                setPromptInstallJava,
                setPendingQuickPlayTarget,
                pendingQuickPlayTarget
            )

            InstanceDetailsEvent.OnDeleteClick -> setPromptDeleteConfirmation(true)
            is InstanceDetailsEvent.OnConfirmDelete -> handleConfirmDelete(event, setPromptDeleteConfirmation)
            InstanceDetailsEvent.OnCopyClick -> setCopyInstanceDialog(
                copyInstanceDialogFactory.create(CopyInstanceTarget(screen.instanceId, currentInstance?.name.orEmpty()))
            )
            is InstanceDetailsEvent.OnCopyInstanceResult -> {
                setCopyInstanceDialog(null)
                if (event.result is CopyInstanceResult.Copied) navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
            }
            InstanceDetailsEvent.OnExportClick -> setExportInstanceDialog(
                exportInstanceDialogFactory.create(
                    ExportInstanceTarget(
                        screen.instanceId,
                        currentInstance?.name.orEmpty()
                    )
                )
            )

            is InstanceDetailsEvent.OnExportInstanceResult -> setExportInstanceDialog(null)
            InstanceDetailsEvent.OnCopyOptionsClick -> currentInstance?.let {
                setCopyOptionsDialog(
                    copyOptionsDialogFactory.create(
                        CopyOptionsTarget(
                            screen.instanceId,
                            it.gameVersion
                        )
                    )
                )
            }

            is InstanceDetailsEvent.OnCopyOptionsResult -> setCopyOptionsDialog(null)
            InstanceDetailsEvent.OnSaveDefaultOptionsClick -> setSaveDefaultOptionsDialog(
                saveDefaultOptionsDialogFactory.create(SaveDefaultOptionsTarget(screen.instanceId))
            )

            is InstanceDetailsEvent.OnSaveDefaultOptionsResult -> setSaveDefaultOptionsDialog(null)
            InstanceDetailsEvent.OnCancelClick -> cancelInstance(screen.instanceId)
            InstanceDetailsEvent.OnCancelSetupClick -> cancelInstanceSetup(screen.instanceId)
            InstanceDetailsEvent.OnStopClick -> globalScope.launch { stopInstance(screen.instanceId) }
            InstanceDetailsEvent.OnKillClick -> globalScope.launch { killInstance(screen.instanceId) }
            is InstanceDetailsEvent.OnTabClick -> setSelectedTab(event.tab)

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

            is InstanceDetailsEvent.OnIconSelected -> handleIconSelected(event)
            InstanceDetailsEvent.OnRemoveIconClick -> globalScope.launch {
                either { removeInstanceIcon(screen.instanceId) }.onLeft { error -> snackbarService.error { error.message } }
            }

            InstanceDetailsEvent.OnRestoreIconClick -> handleRestoreIconClick()
            is InstanceDetailsEvent.OnOpenInstanceFolderClick -> handleOpenInstanceFolderClick(event)

            InstanceDetailsEvent.OnFinishInstallationClick -> {
                setPromptFinishInstallation(true)
                currentInstance?.let(::resumeManualDownloadsIfNeeded)
            }

            InstanceDetailsEvent.OnFinishInstallationDismiss -> setPromptFinishInstallation(false)
            InstanceDetailsEvent.OnResumeInstallationClick -> currentInstance?.let(::resumeInstallationIfNeeded)
            InstanceDetailsEvent.OnUpdateModpackClick -> setPromptModpackUpdate(true)
            is InstanceDetailsEvent.OnConfirmModpackUpdate -> handleConfirmModpackUpdate(
                event = event,
                currentInstance = currentInstance,
                modpackUpdate = modpackUpdate,
                setPromptModpackUpdate = setPromptModpackUpdate,
            )

            is InstanceDetailsEvent.OnUpdateFromFileSelected ->
                handleUpdateFromFileSelected(event, setLocalPackUpdatePrompt)

            is InstanceDetailsEvent.OnConfirmLocalPackUpdate -> handleConfirmLocalPackUpdate(
                event = event,
                currentInstance = currentInstance,
                localPackUpdatePrompt = localPackUpdatePrompt,
                setLocalPackUpdatePrompt = setLocalPackUpdatePrompt,
            )
        }
    }

    private fun handleUpdateFromFileSelected(
        event: InstanceDetailsEvent.OnUpdateFromFileSelected,
        setLocalPackUpdatePrompt: (LocalPackUpdatePrompt?) -> Unit,
    ) {
        globalScope.launch {
            either { parseLocalPack(event.sourcePath) }
                .onLeft { error -> snackbarService.error { error.message } }
                .onRight { pack -> setLocalPackUpdatePrompt(pack.toUpdatePrompt(getInstalledPack(screen.instanceId))) }
        }
    }

    private fun handleConfirmLocalPackUpdate(
        event: InstanceDetailsEvent.OnConfirmLocalPackUpdate,
        currentInstance: InstanceView?,
        localPackUpdatePrompt: LocalPackUpdatePrompt?,
        setLocalPackUpdatePrompt: (LocalPackUpdatePrompt?) -> Unit,
    ) {
        setLocalPackUpdatePrompt(null)
        if (!event.result || currentInstance == null || localPackUpdatePrompt == null) return
        if (hasActiveInstanceJob(screen.instanceId)) return
        localPackUpdateLauncher.launch(screen.instanceId, currentInstance.name, localPackUpdatePrompt.pack)
    }

    private fun handleConfirmModpackUpdate(
        event: InstanceDetailsEvent.OnConfirmModpackUpdate,
        currentInstance: InstanceView?,
        modpackUpdate: ModpackUpdate?,
        setPromptModpackUpdate: (Boolean) -> Unit,
    ) {
        setPromptModpackUpdate(false)
        if (!event.result || currentInstance == null || modpackUpdate == null) return
        if (hasActiveInstanceJob(screen.instanceId)) return

        val instanceId = screen.instanceId
        val job = globalScope.launch {
            val ok = either {
                updateModpack(
                    instanceId = instanceId,
                    update = modpackUpdate,
                    onManualDownloadError = { error -> snackbarService.error { error.message } },
                )
            }.onLeft { error -> snackbarService.error { error.message } }.getOrNull() ?: return@launch
            updateInstanceStatus(instanceId, if (ok) InstanceStatus.Idle else InstanceStatus.Failed.InstallFailed)
            notifyModpackInstallResult(currentInstance.name, ok)
        }
        updateInstanceJob(instanceId, job)
        job.invokeOnCompletion { updateInstanceJob(instanceId, null) }
    }

    private fun handleIconSelected(event: InstanceDetailsEvent.OnIconSelected) {
        globalScope.launch {
            either { setInstanceIcon(screen.instanceId, event.path) }
                .onLeft { error -> snackbarService.error { error.message } }
        }
    }

    private fun handleRestoreIconClick() {
        globalScope.launch {
            recover({
                val instance = getInstance(screen.instanceId)
                val modpackInfo = instance.modpackInfo
                if (modpackInfo != null) {
                    recover({
                        val logoUrl = getProject(modpackInfo.projectId from modpackInfo.platformId).logoUrl
                        if (logoUrl == null) {
                            snackbarService.error { stringResource(InstanceRes.string.errorRestoreIconFailed) }
                        } else {
                            recover({ setInstanceIconFromUrl(instance, logoUrl) }) { _: InstanceIconError ->
                                snackbarService.error { stringResource(InstanceRes.string.errorRestoreIconFailed) }
                            }
                        }
                    }) { _: ProjectDetailsError ->
                        snackbarService.error { stringResource(InstanceRes.string.errorRestoreIconFailed) }
                    }
                }
            }) { _: InstanceError ->
                snackbarService.error { stringResource(InstanceRes.string.errorRestoreIconFailed) }
            }
        }
    }

    private fun handleOpenInstanceFolderClick(event: InstanceDetailsEvent.OnOpenInstanceFolderClick) {
        globalScope.launch {
            either { openInstanceFolder(screen.instanceId, event.folder) }
                .onLeft { snackbarService.error { stringResource(InstanceRes.string.errorOpenFolderFailed) } }
        }
    }

    private fun resumeInstallationIfNeeded(instance: InstanceView) {
        if (instance.status !is InstanceStatus.Installing.Interrupted) return
        val modpackInfo = instance.modpackInfo ?: return
        if (hasActiveInstanceJob(screen.instanceId)) return

        val instanceId = screen.instanceId
        val job = globalScope.launch {
            val ok = runModpackInstall(
                instanceId = instanceId,
                modpackInfo = modpackInfo,
                initialManifest = null,
                onManualDownloadError = { error -> snackbarService.error { error.message } },
            )
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
                candidates = candidates,
                onError = { error -> snackbarService.error { error.message } },
            ) { items -> trackPendingManualDownloads(instanceId, candidates, items) }
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
        setPendingQuickPlayTarget: (QuickPlayTarget?) -> Unit,
        quickPlayTarget: QuickPlayTarget? = null,
    ) = globalScope.launch {
        setPendingQuickPlayTarget(quickPlayTarget)
        val switchToLogsJob = launch {
            val logsFlow = observeInstanceLogs(screen.instanceId)
            logsFlow.first { it != null }
            setSelectedTab(InstanceDetailsTab.Logs)
        }
        either { launchInstance(screen.instanceId, quickPlayTarget) }.onLeft { error ->
            switchToLogsJob.cancel()
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
    ) = startLoginFlow(globalScope, login, loginState, setPromptLogin, setLoginJob)

    private fun handleCancelLogin(
        loginJob: Job?,
        loginState: MutableState<LoginStateView>,
        setPromptLogin: (Boolean) -> Unit,
        setLoginJob: (Job?) -> Unit,
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        setPromptInstallJava: (Int?) -> Unit,
        setPendingQuickPlayTarget: (QuickPlayTarget?) -> Unit,
        pendingQuickPlayTarget: QuickPlayTarget?,
    ) {
        loginJob?.cancel(CancellationException("Login cancelled by user"))
        setPromptLogin(false)
        if (loginState.value.success) {
            handleLaunch(
                setSelectedTab, setPromptInstallJava, loginState, setPromptLogin, setLoginJob,
                setPendingQuickPlayTarget, pendingQuickPlayTarget
            )
        }
    }

    private fun handleInstallJavaConfirm(
        promptInstallJava: Int?,
        setPromptInstallJava: (Int?) -> Unit,
        javaInstallState: MutableState<JavaInstallProgressView>,
        setPromptJavaInstallProgress: (Boolean) -> Unit,
        setJavaInstallJob: (Job?) -> Unit,
    ) {
        setPromptInstallJava(null)
        val majorVersion = promptInstallJava ?: return
        javaInstallState.value = JavaInstallProgressView(majorVersion = majorVersion)
        setPromptJavaInstallProgress(true)
        val job = globalScope.launch {
            coroutineScope {
                val progressJob = launch {
                    observeJavaRuntimeInstallProgress().collect { byVersion ->
                        val status = byVersion[majorVersion]
                        if (status is JavaRuntimeInstallStatus.Downloading) {
                            javaInstallState.update { copy(download = status.download) }
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
        setPromptJavaInstallProgress: (Boolean) -> Unit,
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        setPromptInstallJava: (Int?) -> Unit,
        loginState: MutableState<LoginStateView>,
        setPromptLogin: (Boolean) -> Unit,
        setLoginJob: (Job?) -> Unit,
        setPendingQuickPlayTarget: (QuickPlayTarget?) -> Unit,
        pendingQuickPlayTarget: QuickPlayTarget?,
    ) {
        javaInstallJob?.cancel()
        setPromptJavaInstallProgress(false)
        val succeeded = javaInstallState.value.done && javaInstallState.value.error == null
        if (succeeded) {
            handleLaunch(
                setSelectedTab, setPromptInstallJava, loginState, setPromptLogin, setLoginJob,
                setPendingQuickPlayTarget, pendingQuickPlayTarget
            )
        }
    }

    private fun handleConfirmDelete(
        event: InstanceDetailsEvent.OnConfirmDelete,
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

    @Composable
    private fun observeInstanceView(): State<ProgressiveOutcome<InstanceError, InstanceView>> =
        produceAndCollectAsProgressiveState(screen.instanceId) {
            observeInstance(screen.instanceId).map { outcome -> outcome.map { it.toView() } }
        }

    @CircuitInject(InstanceDetailsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: InstanceDetailsScreen, navigator: Navigator): InstanceDetailsPresenter
    }
}
