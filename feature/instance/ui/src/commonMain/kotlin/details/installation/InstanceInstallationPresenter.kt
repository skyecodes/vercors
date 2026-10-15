package app.vercors.feature.instance.ui.details.installation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.instance.ui.details.InstanceView
import app.vercors.feature.instance.ui.details.observeInstanceView
import app.vercors.feature.instance.ui.install.FinishInstallationDialog
import app.vercors.feature.instance.ui.install.notifyModpackInstallResult
import app.vercors.feature.instance.ui.message
import app.vercors.feature.project.domain.model.toManualDownloadCandidate
import app.vercors.feature.project.domain.usecase.RunManualDownloadsUseCase
import app.vercors.feature.project.domain.usecase.RunModpackInstallUseCase
import app.vercors.feature.project.domain.usecase.TrackPendingManualDownloadsUseCase
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull

@AssistedInject
class InstanceInstallationPresenter(
    @Assisted private val instanceId: InstanceId,
    private val globalScope: CoroutineScope,
    private val observeInstance: ObserveInstanceUseCase,
    private val observeManualDownloadItems: ObserveManualDownloadItemsUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val hasActiveInstanceJob: HasActiveInstanceJobUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val runModpackInstall: RunModpackInstallUseCase,
    private val runManualDownloads: RunManualDownloadsUseCase,
    private val trackPendingManualDownloads: TrackPendingManualDownloadsUseCase,
    private val snackbarService: SnackbarService,
) : Presenter<InstanceInstallationState, InstanceInstallationEvent> {
    @Composable
    override fun present(): Store<InstanceInstallationState, InstanceInstallationEvent> {
        val instance by observeInstanceView(observeInstance, instanceId)
        val (promptFinishInstallation, setPromptFinishInstallation) = remember { mutableStateOf(false) }
        val manualDownloadItems = produceAndCollectAsRetainedState(instanceId, initial = emptyList()) {
            observeManualDownloadItems(instanceId)
        }
        val finishInstallationDialog = remember(instanceId) {
            FinishInstallationDialog(manualDownloadItems) { url -> copyToClipboard(url) }
        }
        val currentInstance = instance.valueOrNull
        return InstanceInstallationState(
            promptFinishInstallation = promptFinishInstallation,
            finishInstallationDialog = finishInstallationDialog,
        ) + { event ->
            handleEvent(
                event = event,
                currentInstance = currentInstance,
                setPromptFinishInstallation = setPromptFinishInstallation,
            )
        }
    }

    private fun handleEvent(
        event: InstanceInstallationEvent,
        currentInstance: InstanceView?,
        setPromptFinishInstallation: (Boolean) -> Unit,
    ) {
        when (event) {
            InstanceInstallationEvent.OnFinishInstallationClick -> {
                setPromptFinishInstallation(true)
                currentInstance?.let(::resumeManualDownloadsIfNeeded)
            }

            InstanceInstallationEvent.OnFinishInstallationDismiss -> setPromptFinishInstallation(false)
            InstanceInstallationEvent.OnResumeInstallationClick -> currentInstance?.let(::resumeInstallationIfNeeded)
        }
    }

    private fun resumeInstallationIfNeeded(instance: InstanceView) {
        if (instance.status !is InstanceStatus.Installing.Interrupted) return
        val modpackInfo = instance.modpackInfo ?: return
        if (hasActiveInstanceJob(instanceId)) return

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
        if (hasActiveInstanceJob(instanceId)) return

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

    @AssistedFactory
    fun interface Factory {
        fun create(instanceId: InstanceId): InstanceInstallationPresenter
    }
}
