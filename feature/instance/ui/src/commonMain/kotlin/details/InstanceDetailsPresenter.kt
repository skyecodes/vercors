package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.core.ui.state.update
import app.vercors.feature.account.domain.usecase.LoginUseCase
import app.vercors.feature.account.ui.list.LoginDialog
import app.vercors.feature.account.ui.list.LoginStateView
import app.vercors.feature.account.ui.list.startLoginFlow
import app.vercors.feature.game.domain.model.JavaRuntimeInstallStatus
import app.vercors.feature.game.domain.usecase.InstallJavaRuntimeUseCase
import app.vercors.feature.game.domain.usecase.ObserveJavaRuntimeInstallProgressUseCase
import app.vercors.feature.instance.domain.model.InstanceError
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
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import arrow.core.raise.recover
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

@AssistedInject
class InstanceDetailsPresenter(
    @Assisted private val screen: InstanceDetailsScreen,
    @Assisted private val navigator: Navigator,
    private val globalScope: CoroutineScope,
    private val observeInstance: ObserveInstanceUseCase,
    private val installedContentPresenterFactory: InstalledContentPresenter.Factory,
    private val instanceLogsPresenterFactory: InstanceLogsPresenter.Factory,
    private val instanceOptionsPresenterFactory: InstanceOptionsPresenter.Factory,
    private val launchInstance: LaunchInstanceUseCase,
    private val cancelInstance: CancelInstanceUseCase,
    private val cancelInstanceSetup: CancelInstanceSetupUseCase,
    private val stopInstance: StopInstanceUseCase,
    private val killInstance: KillInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val snackbarService: SnackbarService,
    private val updateInstance: UpdateInstanceUseCase,
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
    private val openInstanceFolder: OpenInstanceFolderUseCase,
) : Presenter<InstanceDetailsState, InstanceDetailsEvent> {
    @Composable
    override fun present(): Store<InstanceDetailsState, InstanceDetailsEvent> {
        val instance by observeInstanceView()
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
        val logsContent = remember(screen.instanceId) {
            instanceLogsPresenterFactory.create(screen.instanceId)
        }.present()

        val (promptDeleteConfirmation, setPromptDeleteConfirmation) = rememberSaveable { mutableStateOf(false) }
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
        val currentInstance = instance.valueOrNull

        LaunchedEffect(screen.instanceId, screen.autoLaunch) {
            if (screen.autoLaunch) {
                handleLaunch(setSelectedTab, setPromptInstallJava, loginState, setPromptLogin, setLoginJob)
            }
        }

        return InstanceDetailsState(
            instance = instance,
            promptInstallJava = promptInstallJava,
            promptDeleteConfirmation = promptDeleteConfirmation,
            selectedTab = selectedTab,
            optionsContent = optionsContent,
            modsContent = modsContent,
            resourcePacksContent = resourcePacksContent,
            shaderPacksContent = shaderPacksContent,
            logsContent = logsContent,
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
                currentInstance = currentInstance,
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
                setJavaInstallJob = setJavaInstallJob
            )
        }
    }

    private fun handleEvent(
        event: InstanceDetailsEvent,
        setPromptDeleteConfirmation: (Boolean) -> Unit,
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
        setPromptJavaInstall: (Boolean) -> Unit,
        javaInstallJob: Job?,
        setJavaInstallJob: (Job?) -> Unit
    ) {
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
            is InstanceDetailsEvent.SelectTab -> setSelectedTab(event.tab)

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
        }
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
    }

    private fun handleOpenInstanceFolderClick(event: InstanceDetailsEvent.OnOpenInstanceFolderClick) {
        globalScope.launch {
            val opened = openInstanceFolder(screen.instanceId, event.folder)
            if (!opened) snackbarService.error { stringResource(InstanceRes.string.errorOpenFolderFailed) }
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
    ) = startLoginFlow(globalScope, login, loginState, setPromptLogin, setLoginJob)

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
