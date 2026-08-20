package app.vercors.feature.project.ui.install

import androidx.compose.runtime.*
import app.vercors.core.ui.state.mutableProgressiveStateOf
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.model.modpack.InstalledModpackFile
import app.vercors.feature.project.domain.model.modpack.ModpackFileKind
import app.vercors.feature.project.domain.model.modpack.ModpackInstallError
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import app.vercors.feature.project.domain.usecase.AwaitManualDownloadUseCase
import app.vercors.feature.project.domain.usecase.GetModpackManifestUseCase
import app.vercors.feature.project.domain.usecase.GetProjectFilesUseCase
import app.vercors.feature.project.domain.usecase.InstallModpackFilesUseCase
import app.vercors.feature.project.ui.message
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.recover
import com.slack.circuit.overlay.OverlayNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.valueOrNull

class InstallModpackPresenter(
    private val navigator: OverlayNavigator<InstallModpackResult>,
    private val projectId: ProjectId,
    private val projectName: String,
    private val globalScope: CoroutineScope,
    private val getProjectFiles: GetProjectFilesUseCase,
    private val getModpackManifest: GetModpackManifestUseCase,
    private val createInstance: CreateInstanceUseCase,
    private val installModpackFiles: InstallModpackFilesUseCase,
    private val awaitManualDownload: AwaitManualDownloadUseCase,
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val deleteInstanceFiles: DeleteInstanceFilesUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val snackbarService: SnackbarService
) : Presenter<InstallModpackState, InstallModpackEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<InstallModpackState, InstallModpackEvent> {
        var selectedFileId by remember { mutableStateOf<String?>(null) }
        var instanceName by remember { mutableStateOf(projectName) }
        var installing by remember { mutableStateOf(false) }
        var installJob by remember { mutableStateOf<Job?>(null) }
        var manualDownloadItems by remember { mutableStateOf<List<ManualDownloadItemView>>(emptyList()) }
        val files = remember { mutableProgressiveStateOf<ProjectDetailsError, List<ProjectFileView>>() }

        LaunchedEffect(projectId) {
            files.value = ProgressiveOutcome.Incomplete()
            val outcome = progressive { getProjectFiles(projectId).map { it.toView() } }
            files.value = outcome
            selectedFileId = outcome.valueOrNull?.let(::pickRecommendedFile)?.id
        }

        fun handleEvent(event: InstallModpackEvent) {
            when (event) {
                is InstallModpackEvent.OnFileSelected -> selectedFileId = event.fileId
                is InstallModpackEvent.OnInstanceNameChange -> instanceName = event.name
                is InstallModpackEvent.OnCopyUrlClick -> copyToClipboard(event.url)
                InstallModpackEvent.OnCancelClick -> {
                    val job = installJob
                    if (job != null && job.isActive) job.cancel() else navigator.finish(InstallModpackResult.Cancelled)
                }

                InstallModpackEvent.OnConfirmClick -> {
                    val file = files.value.valueOrNull?.find { it.id == selectedFileId }
                    if (file != null && instanceName.length >= 3) {
                        installing = true
                        installJob = confirmInstall(
                            file, instanceName,
                            onManualDownloadItemsChanged = { manualDownloadItems = it }
                        ) { instanceId ->
                            installing = false
                            manualDownloadItems = emptyList()
                            navigator.finish(
                                if (instanceId != null) InstallModpackResult.Installed(instanceId)
                                else InstallModpackResult.Cancelled
                            )
                        }
                    }
                }
            }
        }

        return InstallModpackState(
            files = files.value,
            selectedFileId = selectedFileId,
            instanceName = instanceName,
            installing = installing,
            manualDownloadItems = manualDownloadItems
        ) + ::handleEvent
    }

    /** Resolves the modpack, creates its instance, installs its files (prompting for manual downloads as needed), and records provenance — all-or-nothing. */
    private fun confirmInstall(
        file: ProjectFileView,
        instanceName: String,
        onManualDownloadItemsChanged: (List<ManualDownloadItemView>) -> Unit,
        onFinished: (InstanceId?) -> Unit
    ): Job {
        var createdInstanceId: InstanceId? = null
        val job = globalScope.launch {
            val manifest = recover({ getModpackManifest(projectId, file.id) }) { error: ModpackResolutionError ->
                snackbarService.error { error.message }
                null
            }
            if (manifest == null) {
                onFinished(null)
                return@launch
            }

            val instance = Instance(
                name = instanceName,
                gameVersion = manifest.gameVersion,
                modLoader = manifest.loaderType?.let {
                    InstanceModLoader(
                        type = it,
                        version = manifest.loaderVersion!!
                    )
                },
                modpackInfo = InstanceModpackInfo(manifest.platformId, manifest.projectId, manifest.fileId)
            )
            val created = recover({ createInstance(instance); true }) { error: InstanceValidationError ->
                snackbarService.error { error.message }
                false
            }
            if (!created) {
                onFinished(null)
                return@launch
            }
            createdInstanceId = instance.id

            val installedFiles = recover({
                installModpackFiles(manifest, instance.id) {}
            }) { error: ModpackInstallError ->
                snackbarService.error { error.message }
                null
            }
            if (installedFiles == null) {
                rollback(instance.id)
                onFinished(null)
                return@launch
            }

            recordOrigins(instance.id, manifest.platformId, installedFiles)

            val manualCandidates = manifest.files.mapNotNull { manualFile ->
                val manualDownloadUrl = manualFile.manualDownloadUrl ?: return@mapNotNull null
                val providerProjectId = manualFile.providerProjectId ?: return@mapNotNull null
                val providerFileId = manualFile.providerFileId ?: return@mapNotNull null
                val fileName = manualFile.targetPath.substringAfterLast('/')
                ManualDownloadCandidate(
                    fileName = fileName,
                    downloadUrl = manualDownloadUrl,
                    instanceId = instance.id,
                    projectType = ProjectType.Mod,
                    recordOrigin = {
                        recordInstalledContentOrigin(
                            ContentKind.Mod,
                            instance.id,
                            fileName,
                            manifest.platformId,
                            providerProjectId,
                            providerFileId
                        )
                    }
                )
            }
            val manualOk =
                runManualDownloads(manualCandidates, awaitManualDownload, snackbarService, onManualDownloadItemsChanged)
            if (!manualOk) {
                rollback(instance.id)
                onFinished(null)
                return@launch
            }

            onFinished(instance.id)
        }
        // Only reached when the job was actually cancelled (e.g. mid manual-download wait) — logical failures
        // above already roll back explicitly and complete normally, so this never double-rolls-back.
        job.invokeOnCompletion { error ->
            if (error != null) createdInstanceId?.let { id -> globalScope.launch { rollback(id) } }
        }
        return job
    }

    private suspend fun rollback(instanceId: InstanceId) {
        recover({ deleteInstance(instanceId) }) { _: InstanceError -> null }
        deleteInstanceFiles(instanceId)
    }

    private suspend fun recordOrigins(
        instanceId: InstanceId,
        platformId: String,
        installedFiles: List<InstalledModpackFile>
    ) {
        for (installedFile in installedFiles) {
            val kind = installedFile.kind.toContentKindOrNull() ?: continue
            val providerProjectId = installedFile.providerProjectId ?: continue
            val providerFileId = installedFile.providerFileId ?: continue
            recordInstalledContentOrigin(
                kind,
                instanceId,
                installedFile.fileName,
                platformId,
                providerProjectId,
                providerFileId
            )
        }
    }
}

private fun ModpackFileKind.toContentKindOrNull(): ContentKind? = when (this) {
    ModpackFileKind.Mod -> ContentKind.Mod
    ModpackFileKind.ResourcePack -> ContentKind.ResourcePack
    ModpackFileKind.ShaderPack -> ContentKind.ShaderPack
    ModpackFileKind.Other -> null
}
