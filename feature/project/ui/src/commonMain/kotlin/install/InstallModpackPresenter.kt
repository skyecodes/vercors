package app.vercors.feature.project.ui.install

import androidx.compose.runtime.*
import app.vercors.core.domain.toDownloadProgressOrNull
import app.vercors.core.domain.toFractionOrNull
import app.vercors.core.ui.state.mutableProgressiveStateOf
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceJobUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceStatusUseCase
import app.vercors.feature.instance.ui.install.notifyModpackInstallResult
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import app.vercors.feature.project.domain.usecase.ApplyProjectIconUseCase
import app.vercors.feature.project.domain.usecase.GetModpackManifestUseCase
import app.vercors.feature.project.domain.usecase.GetProjectFilesUseCase
import app.vercors.feature.project.domain.usecase.RunModpackInstallUseCase
import app.vercors.feature.project.ui.message
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.recover
import com.slack.circuit.overlay.OverlayNavigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.valueOrNull
import app.vercors.feature.instance.ui.message as manualDownloadMessage

@AssistedInject
class InstallModpackPresenter(
    @Assisted private val navigator: OverlayNavigator<InstallModpackResult>,
    @Assisted private val projectId: ProjectId,
    @Assisted private val projectName: String,
    private val globalScope: CoroutineScope,
    private val getProjectFiles: GetProjectFilesUseCase,
    private val getModpackManifest: GetModpackManifestUseCase,
    private val createInstance: CreateInstanceUseCase,
    private val applyProjectIcon: ApplyProjectIconUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val runModpackInstall: RunModpackInstallUseCase,
    private val snackbarService: SnackbarService
) : Presenter<InstallModpackState, InstallModpackEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<InstallModpackState, InstallModpackEvent> {
        val (selectedFileId, setSelectedFileId) = remember { mutableStateOf<String?>(null) }
        val (instanceName, setInstanceName) = remember { mutableStateOf(projectName) }
        val (installing, setInstalling) = remember { mutableStateOf(false) }
        val (installProgress, setInstallProgress) = remember { mutableStateOf(InstallProgressView()) }
        val (installJob, setInstallJob) = remember { mutableStateOf<Job?>(null) }
        val files = remember { mutableProgressiveStateOf<ProjectDetailsError, List<ProjectFileView>>() }

        LaunchedEffect(projectId) {
            files.value = ProgressiveOutcome.Incomplete()
            val outcome = progressive { getProjectFiles(projectId).map { it.toView() } }
            files.value = outcome
            setSelectedFileId(outcome.valueOrNull?.let(::pickRecommendedFile)?.id)
        }

        return InstallModpackState(
            files = files.value,
            selectedFileId = selectedFileId,
            instanceName = instanceName,
            installing = installing,
            installProgress = installProgress
        ) + { event ->
            handleEvent(
                event = event,
                files = files,
                selectedFileId = selectedFileId,
                setSelectedFileId = setSelectedFileId,
                instanceName = instanceName,
                setInstanceName = setInstanceName,
                installJob = installJob,
                setInstallJob = setInstallJob,
                setInstalling = setInstalling,
                setInstallProgress = setInstallProgress,
            )
        }
    }

    private fun handleEvent(
        event: InstallModpackEvent,
        files: MutableState<ProgressiveOutcome<ProjectDetailsError, List<ProjectFileView>>>,
        selectedFileId: String?,
        setSelectedFileId: (String?) -> Unit,
        instanceName: String,
        setInstanceName: (String) -> Unit,
        installJob: Job?,
        setInstallJob: (Job?) -> Unit,
        setInstalling: (Boolean) -> Unit,
        setInstallProgress: (InstallProgressView) -> Unit,
    ) {
        when (event) {
            is InstallModpackEvent.OnFileSelected -> setSelectedFileId(event.fileId)
            is InstallModpackEvent.OnInstanceNameChange -> setInstanceName(event.name)
            InstallModpackEvent.OnCancelClick -> {
                installJob?.cancel()
                navigator.finish(InstallModpackResult.Cancelled)
            }

            InstallModpackEvent.OnConfirmClick -> handleConfirmClick(
                files, selectedFileId, instanceName, setInstalling, setInstallProgress, setInstallJob
            )
        }
    }

    private fun handleConfirmClick(
        files: MutableState<ProgressiveOutcome<ProjectDetailsError, List<ProjectFileView>>>,
        selectedFileId: String?,
        instanceName: String,
        setInstalling: (Boolean) -> Unit,
        setInstallProgress: (InstallProgressView) -> Unit,
        setInstallJob: (Job?) -> Unit,
    ) {
        val file = files.value.valueOrNull?.find { it.id == selectedFileId }
        if (file != null && instanceName.length >= 3) {
            setInstalling(true)
            setInstallProgress(InstallProgressView())
            setInstallJob(
                confirmInstall(
                    file, instanceName,
                    onProgressChanged = { setInstallProgress(it) },
                    onInstanceCreated = { instanceId ->
                        setInstalling(false)
                        navigator.finish(InstallModpackResult.Installed(instanceId))
                    },
                    onFinished = {
                        setInstalling(false)
                        navigator.finish(InstallModpackResult.Cancelled)
                    }
                )
            )
        }
    }

    // Failures after the instance exists leave it in place as Failed; only an explicit cancellation rolls it back
    private fun confirmInstall(
        file: ProjectFileView,
        instanceName: String,
        onProgressChanged: (InstallProgressView) -> Unit,
        onInstanceCreated: (InstanceId) -> Unit,
        onFinished: () -> Unit
    ): Job {
        var createdInstanceId: InstanceId? = null
        lateinit var job: Job
        job = globalScope.launch {
            val manifest = resolveManifestOrNull(file, onProgressChanged) ?: run {
                onFinished()
                return@launch
            }

            val modpackInfo =
                InstanceModpackInfo(manifest.platformId, manifest.projectId, manifest.fileId, file.displayName)
            val instance = tryCreateInstance(buildInstance(manifest, modpackInfo, instanceName)) ?: run {
                onFinished()
                return@launch
            }
            createdInstanceId = instance.id
            updateInstanceJob(instance.id, job)
            onInstanceCreated(instance.id)

            // Child of `job`, not `globalScope`, so it's cancelled automatically if the install is rolled back.
            launch { applyProjectIcon(instance, projectId) }

            val ok = runModpackInstall(
                instanceId = instance.id,
                modpackInfo = modpackInfo,
                initialManifest = manifest,
                onManualDownloadError = { error -> snackbarService.error { error.manualDownloadMessage } },
            )
            updateInstanceStatus(instance.id, if (ok) InstanceStatus.Idle else InstanceStatus.Failed.InstallFailed)
            notifyModpackInstallResult(instanceName, ok)
        }
        // Reached with an error only on cancellation or an unexpected exception: step failures complete normally
        job.invokeOnCompletion { error ->
            createdInstanceId?.let { updateInstanceJob(it, null) }
            if (error != null) createdInstanceId?.let { id -> globalScope.launch { rollback(id) } }
        }
        return job
    }

    private suspend fun resolveManifestOrNull(
        file: ProjectFileView,
        onProgressChanged: (InstallProgressView) -> Unit
    ): ModpackManifest? =
        recover({
            getModpackManifest(projectId, file.id) { progress ->
                onProgressChanged(
                    InstallProgressView(progress.toFractionOrNull() ?: 0f, progress.toDownloadProgressOrNull())
                )
            }
        }) { error: ModpackResolutionError ->
            snackbarService.error { error.message }
            null
        }

    private fun buildInstance(
        manifest: ModpackManifest,
        modpackInfo: InstanceModpackInfo,
        instanceName: String
    ) = Instance(
        name = instanceName,
        gameVersion = manifest.gameVersion,
        modLoader = manifest.loaderType?.let {
            InstanceModLoader(
                type = it,
                version = manifest.loaderVersion!!
            )
        },
        modpackInfo = modpackInfo,
        installStage = InstanceInstallStage.ResolvingFiles,
    )

    private suspend fun tryCreateInstance(instance: Instance): Instance? =
        recover({ createInstance(instance) }) { error: InstanceValidationError ->
            snackbarService.error { error.message }
            null
        }


    private suspend fun rollback(instanceId: InstanceId) {
        recover({ deleteInstance(instanceId) }) { _: InstanceError -> null }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            navigator: OverlayNavigator<InstallModpackResult>,
            projectId: ProjectId,
            projectName: String
        ): InstallModpackPresenter
    }
}
