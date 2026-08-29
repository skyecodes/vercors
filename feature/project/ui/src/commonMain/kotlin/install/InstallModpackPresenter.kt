package app.vercors.feature.project.ui.install

import androidx.compose.runtime.*
import app.vercors.core.ui.state.mutableProgressiveStateOf
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.instance.ui.install.ModpackInstallRunner
import app.vercors.feature.instance.ui.install.notifyModpackInstallResult
import app.vercors.feature.instance.ui.install.toFractionOrNull
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import app.vercors.feature.project.domain.usecase.GetModpackManifestUseCase
import app.vercors.feature.project.domain.usecase.GetProjectFilesUseCase
import app.vercors.feature.project.domain.usecase.GetProjectUseCase
import app.vercors.feature.project.ui.message
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.recover
import com.slack.circuit.overlay.OverlayNavigator
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.valueOrNull

private val logger = KotlinLogging.logger {}

class InstallModpackPresenter(
    private val navigator: OverlayNavigator<InstallModpackResult>,
    private val projectId: ProjectId,
    private val projectName: String,
    private val globalScope: CoroutineScope,
    private val getProjectFiles: GetProjectFilesUseCase,
    private val getProject: GetProjectUseCase,
    private val getModpackManifest: GetModpackManifestUseCase,
    private val createInstance: CreateInstanceUseCase,
    private val setInstanceIconFromUrl: SetInstanceIconFromUrlUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val modpackInstallRunner: ModpackInstallRunner,
    private val snackbarService: SnackbarService
) : Presenter<InstallModpackState, InstallModpackEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<InstallModpackState, InstallModpackEvent> {
        val (selectedFileId, setSelectedFileId) = remember { mutableStateOf<String?>(null) }
        val (instanceName, setInstanceName) = remember { mutableStateOf(projectName) }
        val (installing, setInstalling) = remember { mutableStateOf(false) }
        val (installProgress, setInstallProgress) = remember { mutableStateOf<Float?>(null) }
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
        setInstallProgress: (Float?) -> Unit,
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
        setInstallProgress: (Float?) -> Unit,
        setInstallJob: (Job?) -> Unit,
    ) {
        val file = files.value.valueOrNull?.find { it.id == selectedFileId }
        if (file != null && instanceName.length >= 3) {
            setInstalling(true)
            setInstallProgress(null)
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

    /**
     * Parses the pack's own manifest (fast, local-file-only — enough to create the instance), creates the
     * instance, then resolves the rest of the manifest (may be slow: CurseForge needs one network lookup per mod),
     * downloads its files (prompting for manual downloads as needed), and records provenance. [onInstanceCreated]
     * fires as soon as the instance exists, so the caller can navigate to it immediately — the remaining steps keep
     * running on [globalScope] and report their progress via [updateInstanceStatus], surviving the dialog being
     * dismissed. Failures before the instance exists call [onFinished] with nothing to clean up; failures afterward
     * leave the instance in place with an [InstanceStatus.Failed] status instead of rolling it back, since the user
     * may already be looking at it — only an explicit cancellation (job cancelled from the instance details screen)
     * rolls it back.
     */
    private fun confirmInstall(
        file: ProjectFileView,
        instanceName: String,
        onProgressChanged: (Float?) -> Unit,
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

            val instance = tryCreateInstance(buildInstance(manifest, instanceName)) ?: run {
                onFinished()
                return@launch
            }
            createdInstanceId = instance.id
            updateInstanceJob(instance.id, job)
            onInstanceCreated(instance.id)

            // Child of `job`, not `globalScope`, so it's cancelled automatically if the install is rolled back.
            launch { fetchAndApplyProjectIcon(instance) }

            val ok = modpackInstallRunner.run(
                instanceId = instance.id,
                modpackInfo = InstanceModpackInfo(manifest.platformId, manifest.projectId, manifest.fileId),
                initialManifest = manifest,
            )
            updateInstanceStatus(instance.id, if (ok) InstanceStatus.Idle else InstanceStatus.Failed.InstallFailed)
            notifyModpackInstallResult(instanceName, ok)
        }
        // Only reached with a non-null error on genuine cancellation (job.cancel() via CancelInstanceSetupUseCase)
        // or a truly unexpected exception — expected step failures above already report InstanceStatus.Failed and
        // complete normally instead of throwing, so they never reach this rollback path.
        job.invokeOnCompletion { error ->
            createdInstanceId?.let { updateInstanceJob(it, null) }
            if (error != null) createdInstanceId?.let { id -> globalScope.launch { rollback(id) } }
        }
        return job
    }

    private suspend fun resolveManifestOrNull(
        file: ProjectFileView,
        onProgressChanged: (Float?) -> Unit
    ): ModpackManifest? =
        recover({
            getModpackManifest(projectId, file.id) { progress -> onProgressChanged(progress.toFractionOrNull()) }
        }) { error: ModpackResolutionError ->
            snackbarService.error { error.message }
            null
        }

    private fun buildInstance(manifest: ModpackManifest, instanceName: String) = Instance(
        name = instanceName,
        gameVersion = manifest.gameVersion,
        modLoader = manifest.loaderType?.let {
            InstanceModLoader(
                type = it,
                version = manifest.loaderVersion!!
            )
        },
        modpackInfo = InstanceModpackInfo(manifest.platformId, manifest.projectId, manifest.fileId),
        installStage = InstanceInstallStage.ResolvingFiles,
    )

    private suspend fun tryCreateInstance(instance: Instance): Instance? =
        recover({ createInstance(instance) }) { error: InstanceValidationError ->
            snackbarService.error { error.message }
            null
        }

    /** Best-effort only: a missing/broken project logo must never fail or delay the modpack install. */
    private suspend fun fetchAndApplyProjectIcon(instance: Instance) {
        recover({
            val logoUrl = getProject(projectId).logoUrl
            if (logoUrl != null && !setInstanceIconFromUrl(instance, logoUrl)) {
                logger.warn { "Failed to set instance icon for ${instance.id} from $logoUrl" }
            }
        }) { error: ProjectDetailsError ->
            logger.warn { "Failed to fetch modpack project details for instance icon (${instance.id}): $error" }
        }
    }

    private suspend fun rollback(instanceId: InstanceId) {
        recover({ deleteInstance(instanceId) }) { _: InstanceError -> null }
    }
}
