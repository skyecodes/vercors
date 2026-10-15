package app.vercors.feature.instance.ui.details.launch

import androidx.compose.runtime.*
import app.vercors.core.ui.state.update
import app.vercors.feature.account.domain.usecase.LoginUseCase
import app.vercors.feature.account.ui.list.LoginDialog
import app.vercors.feature.account.ui.list.LoginStateView
import app.vercors.feature.account.ui.list.startLoginFlow
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.CancelInstanceJobUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceLogsUseCase
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.details.InstallJavaProgressDialog
import app.vercors.feature.instance.ui.details.JavaInstallProgressView
import app.vercors.feature.instance.ui.javaInstallFailed
import app.vercors.feature.instance.ui.message
import app.vercors.feature.java.domain.model.JavaRuntimeInstallStatus
import app.vercors.feature.java.domain.usecase.InstallJavaRuntimeUseCase
import app.vercors.feature.java.domain.usecase.ObserveJavaRuntimeInstallProgressUseCase
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.model.QuickPlayTarget
import app.vercors.feature.launcher.domain.usecase.KillInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.StopInstanceUseCase
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

@AssistedInject
class InstanceLaunchPresenter(
    @Assisted private val instanceId: InstanceId,
    @Assisted private val autoLaunch: Boolean,
    @Assisted private val onShowLogs: () -> Unit,
    private val globalScope: CoroutineScope,
    private val observeInstanceLogs: ObserveInstanceLogsUseCase,
    private val launchInstance: LaunchInstanceUseCase,
    private val cancelInstanceJob: CancelInstanceJobUseCase,
    private val stopInstance: StopInstanceUseCase,
    private val killInstance: KillInstanceUseCase,
    private val installJavaRuntime: InstallJavaRuntimeUseCase,
    private val observeJavaRuntimeInstallProgress: ObserveJavaRuntimeInstallProgressUseCase,
    private val login: LoginUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val snackbarService: SnackbarService,
) : Presenter<InstanceLaunchState, InstanceLaunchEvent> {
    @Composable
    override fun present(): Store<InstanceLaunchState, InstanceLaunchEvent> {
        val launchFlow = rememberLaunchFlowState()

        LaunchedEffect(instanceId, autoLaunch) {
            if (autoLaunch) handleLaunch(launchFlow)
        }

        return InstanceLaunchState(
            promptInstallJava = launchFlow.promptInstallJava,
            promptLogin = launchFlow.promptLogin,
            loginDialog = launchFlow.loginDialog,
            promptJavaInstallProgress = launchFlow.promptJavaInstallProgress,
            installJavaProgressDialog = launchFlow.installJavaProgressDialog,
        ) + { event ->
            handleEvent(
                event = event,
                launchFlow = launchFlow,
            )
        }
    }

    @Composable
    private fun rememberLaunchFlowState(): LaunchFlowState {
        val (promptInstallJava, setPromptInstallJava) = remember { mutableStateOf<Int?>(null) }
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
        return LaunchFlowState(
            promptInstallJava = promptInstallJava,
            setPromptInstallJava = setPromptInstallJava,
            loginState = loginState,
            promptLogin = promptLogin,
            setPromptLogin = setPromptLogin,
            loginDialog = loginDialog,
            loginJob = loginJob,
            setLoginJob = setLoginJob,
            javaInstallState = javaInstallState,
            promptJavaInstallProgress = promptJavaInstallProgress,
            setPromptJavaInstallProgress = setPromptJavaInstallProgress,
            installJavaProgressDialog = installJavaProgressDialog,
            javaInstallJob = javaInstallJob,
            setJavaInstallJob = setJavaInstallJob,
            pendingQuickPlayTarget = pendingQuickPlayTarget,
            setPendingQuickPlayTarget = setPendingQuickPlayTarget,
        )
    }

    private fun handleEvent(event: InstanceLaunchEvent, launchFlow: LaunchFlowState) {
        when (event) {
            InstanceLaunchEvent.OnLaunchClick -> handleLaunch(launchFlow)
            is InstanceLaunchEvent.OnQuickJoinClick -> handleLaunch(launchFlow, event.target)
            InstanceLaunchEvent.OnCancelClick -> cancelInstanceJob(instanceId)
            InstanceLaunchEvent.OnStopClick -> globalScope.launch { stopInstance(instanceId) }
            InstanceLaunchEvent.OnKillClick -> globalScope.launch { killInstance(instanceId) }
            InstanceLaunchEvent.OnInstallJavaConfirm -> handleInstallJavaConfirm(launchFlow)
            InstanceLaunchEvent.OnInstallJavaDismiss -> launchFlow.setPromptInstallJava(null)
            InstanceLaunchEvent.OnJavaInstallProgressDismiss -> handleJavaInstallDialogDismiss(launchFlow)
            InstanceLaunchEvent.OnLoginDismiss -> handleCancelLogin(launchFlow)
        }
    }

    private fun handleLaunch(
        launchFlow: LaunchFlowState,
        quickPlayTarget: QuickPlayTarget? = null,
    ) = globalScope.launch {
        launchFlow.setPendingQuickPlayTarget(quickPlayTarget)
        val switchToLogsJob = launch {
            observeInstanceLogs(instanceId).first { it != null }
            onShowLogs()
        }
        either { launchInstance(instanceId, quickPlayTarget) }.onLeft { error ->
            switchToLogsJob.cancel()
            when (error) {
                is InstanceLaunchError.NoCompatibleJava -> launchFlow.setPromptInstallJava(error.majorVersion)
                InstanceLaunchError.LoginRequired -> startLoginFlow(
                    globalScope,
                    login,
                    launchFlow.loginState,
                    launchFlow.setPromptLogin,
                    launchFlow.setLoginJob
                )

                else -> snackbarService.error { error.message }
            }
        }
    }

    private fun handleCancelLogin(launchFlow: LaunchFlowState) {
        launchFlow.loginJob?.cancel(CancellationException("Login cancelled by user"))
        launchFlow.setPromptLogin(false)
        if (launchFlow.loginState.value.success) handleLaunch(launchFlow, launchFlow.pendingQuickPlayTarget)
    }

    private fun handleInstallJavaConfirm(launchFlow: LaunchFlowState) {
        launchFlow.setPromptInstallJava(null)
        val majorVersion = launchFlow.promptInstallJava ?: return
        val javaInstallState = launchFlow.javaInstallState
        javaInstallState.value = JavaInstallProgressView(majorVersion = majorVersion)
        launchFlow.setPromptJavaInstallProgress(true)
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
        launchFlow.setJavaInstallJob(job)
    }

    private fun handleJavaInstallDialogDismiss(launchFlow: LaunchFlowState) {
        launchFlow.javaInstallJob?.cancel()
        launchFlow.setPromptJavaInstallProgress(false)
        val javaInstallState = launchFlow.javaInstallState.value
        if (javaInstallState.done && javaInstallState.error == null) {
            handleLaunch(launchFlow, launchFlow.pendingQuickPlayTarget)
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(instanceId: InstanceId, autoLaunch: Boolean, onShowLogs: () -> Unit): InstanceLaunchPresenter
    }
}

private data class LaunchFlowState(
    val promptInstallJava: Int?,
    val setPromptInstallJava: (Int?) -> Unit,
    val loginState: MutableState<LoginStateView>,
    val promptLogin: Boolean,
    val setPromptLogin: (Boolean) -> Unit,
    val loginDialog: LoginDialog,
    val loginJob: Job?,
    val setLoginJob: (Job?) -> Unit,
    val javaInstallState: MutableState<JavaInstallProgressView>,
    val promptJavaInstallProgress: Boolean,
    val setPromptJavaInstallProgress: (Boolean) -> Unit,
    val installJavaProgressDialog: InstallJavaProgressDialog,
    val javaInstallJob: Job?,
    val setJavaInstallJob: (Job?) -> Unit,
    val pendingQuickPlayTarget: QuickPlayTarget?,
    val setPendingQuickPlayTarget: (QuickPlayTarget?) -> Unit,
)
