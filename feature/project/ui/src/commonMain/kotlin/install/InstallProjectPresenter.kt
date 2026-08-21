package app.vercors.feature.project.ui.install

import androidx.compose.runtime.*
import app.vercors.core.domain.filterIsRight
import app.vercors.core.ui.state.mutableProgressiveStateOf
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.ManualDownloadItem
import app.vercors.feature.instance.domain.model.ModProvider
import app.vercors.feature.instance.domain.usecase.ContentKind
import app.vercors.feature.instance.domain.usecase.ObserveInstalledContentUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.domain.usecase.RecordInstalledContentOriginUseCase
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.usecase.AwaitManualDownloadUseCase
import app.vercors.feature.project.domain.usecase.DownloadProjectFileUseCase
import app.vercors.feature.project.domain.usecase.GetProjectFilesUseCase
import app.vercors.feature.project.domain.usecase.GetProjectUseCase
import app.vercors.feature.project.ui.message
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.Raise
import arrow.core.raise.recover
import com.slack.circuit.overlay.OverlayNavigator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull

class InstallProjectPresenter(
    private val navigator: OverlayNavigator<InstallProjectResult>,
    private val projectId: ProjectId,
    private val projectType: ProjectType,
    private val compatibleLoaders: List<ModLoaderType>,
    private val preferredInstanceId: InstanceId?,
    private val globalScope: CoroutineScope,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val observeInstalledContent: ObserveInstalledContentUseCase,
    private val getProject: GetProjectUseCase,
    private val getProjectFiles: GetProjectFilesUseCase,
    private val downloadProjectFile: DownloadProjectFileUseCase,
    private val awaitManualDownload: AwaitManualDownloadUseCase,
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val snackbarService: SnackbarService
) : Presenter<InstallProjectState, InstallProjectEvent> {
    @Composable
    override fun present(): Store<InstallProjectState, InstallProjectEvent> {
        val instances by observeInstanceListView()
        var selectedInstanceId by remember { mutableStateOf(preferredInstanceId) }
        var selectedFileId by remember { mutableStateOf<String?>(null) }
        var dependencies by remember { mutableStateOf<List<DependencyFileView>>(emptyList()) }
        var selectedDependencyIds by remember { mutableStateOf<Set<ProjectId>>(emptySet()) }
        var resolvingDependencies by remember { mutableStateOf(false) }
        var installing by remember { mutableStateOf(false) }
        var installJob by remember { mutableStateOf<Job?>(null) }
        var manualDownloadItems by remember { mutableStateOf<List<ManualDownloadItem>>(emptyList()) }
        val files = remember { mutableProgressiveStateOf<ProjectDetailsError, List<ProjectFileView>>() }

        val selectedInstance = instances.valueOrNull?.find { it.id == selectedInstanceId }
        val selectedFile = files.value.valueOrNull?.find { it.id == selectedFileId }
        val installedProviders by observeInstalledProvidersView(selectedInstanceId)

        LaunchedEffect(selectedInstance?.id) {
            selectedFileId = loadFiles(selectedInstance, files)
        }

        LaunchedEffect(selectedFileId, installedProviders) {
            when (val resolution = resolveDependencies(selectedFile, selectedInstance, installedProviders)) {
                DependencyResolution.NotApplicable -> {
                    dependencies = emptyList()
                    selectedDependencyIds = emptySet()
                    resolvingDependencies = false
                }

                DependencyResolution.Pending -> resolvingDependencies = true

                is DependencyResolution.Resolved -> {
                    dependencies = resolution.dependencies
                    selectedDependencyIds = resolution.dependencies.map { it.projectId }.toSet()
                    resolvingDependencies = false
                }
            }
        }

        fun handleEvent(event: InstallProjectEvent) {
            when (event) {
                is InstallProjectEvent.OnInstanceSelected -> selectedInstanceId = event.instanceId
                is InstallProjectEvent.OnFileSelected -> selectedFileId = event.fileId
                is InstallProjectEvent.OnDependencyToggled -> selectedDependencyIds = if (event.checked) {
                    selectedDependencyIds + event.projectId
                } else {
                    selectedDependencyIds - event.projectId
                }

                is InstallProjectEvent.OnCopyUrlClick -> copyToClipboard(event.url)

                InstallProjectEvent.OnCancelClick -> {
                    val job = installJob
                    if (job != null && job.isActive) job.cancel() else navigator.finish(InstallProjectResult.Cancelled)
                }

                InstallProjectEvent.OnConfirmClick -> {
                    val instanceId = selectedInstanceId
                    val file = files.value.valueOrNull?.find { it.id == selectedFileId }
                    if (instanceId != null && file != null) {
                        val selectedDependencies = dependencies.filter { it.projectId in selectedDependencyIds }
                        installing = true
                        installJob = confirmInstall(
                            instanceId, file, selectedDependencies,
                            onManualDownloadItemsChanged = { manualDownloadItems = it }
                        ) { success ->
                            installing = false
                            manualDownloadItems = emptyList()
                            if (success) navigator.finish(InstallProjectResult.Installed(instanceId))
                        }
                    }
                }
            }
        }

        return InstallProjectState(
            instances = instances,
            selectedInstanceId = selectedInstanceId,
            files = files.value,
            selectedFileId = selectedFileId,
            dependencies = dependencies,
            selectedDependencyIds = selectedDependencyIds,
            resolvingDependencies = resolvingDependencies,
            installing = installing,
            manualDownloadItems = manualDownloadItems
        ) + ::handleEvent
    }

    /** Fetches the compatible files for [instance] and picks a default selection; used for the primary file. */
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    private suspend fun loadFiles(
        instance: InstallableInstanceView?,
        files: MutableState<ProgressiveOutcome<ProjectDetailsError, List<ProjectFileView>>>
    ): String? {
        files.value = ProgressiveOutcome.Incomplete()
        if (instance == null) return null
        val outcome = progressive {
            getProjectFiles(
                projectId,
                instance.gameVersion,
                if (projectType.requiresLoader) instance.loaderType else null
            ).map { it.toView() }
        }
        files.value = outcome
        return outcome.valueOrNull?.let(::pickRecommendedFile)?.id
    }

    /**
     * Resolves [file]'s required dependencies against what's already installed in [instance], skipping any that
     * are already present. Deliberately stays [DependencyResolution.Pending] while [installedProviders] is still
     * loading rather than resolving against a possibly-empty set — otherwise an already-installed dependency can
     * briefly look missing and get re-downloaded if the user confirms before the scan catches up.
     */
    private suspend fun resolveDependencies(
        file: ProjectFileView?,
        instance: InstallableInstanceView?,
        installedProviders: ProgressiveOutcome<Nothing, List<ModProvider>>
    ): DependencyResolution {
        if (file == null || instance == null || file.dependencies.isEmpty()) return DependencyResolution.NotApplicable
        if (installedProviders is ProgressiveOutcome.Incomplete) return DependencyResolution.Pending

        val installedKeys = installedProviders.valueOrNull.orEmpty().map { it.platformId to it.projectId }.toSet()
        val depsToResolve = file.dependencies.filterNot { (it.platform to it.id) in installedKeys }
        if (depsToResolve.isEmpty()) return DependencyResolution.Resolved(emptyList())

        val resolved = coroutineScope {
            depsToResolve.map { dependencyId ->
                async {
                    resolveDependency(
                        dependencyId,
                        instance.gameVersion,
                        if (projectType.requiresLoader) instance.loaderType else null
                    )
                }
            }.awaitAll().filterNotNull()
        }
        return DependencyResolution.Resolved(resolved)
    }

    private suspend fun resolveDependency(
        dependencyId: ProjectId,
        gameVersion: String,
        loader: ModLoaderType?
    ): DependencyFileView? = recover({
        val project = getProject(dependencyId)
        val fileList = getProjectFiles(dependencyId, gameVersion, loader).map { it.toView() }
        pickRecommendedFile(fileList)?.let { DependencyFileView(dependencyId, project.name, it) }
    }) { null }

    /** Installs [file] (and each of [selectedDependencies]), auto files first, then manual ones in parallel. */
    private fun confirmInstall(
        instanceId: InstanceId,
        file: ProjectFileView,
        selectedDependencies: List<DependencyFileView>,
        onManualDownloadItemsChanged: (List<ManualDownloadItem>) -> Unit,
        onFinished: (success: Boolean) -> Unit
    ): Job {
        val candidates = buildList {
            add(InstallCandidate(projectId, file))
            selectedDependencies.forEach { add(InstallCandidate(it.projectId, it.file)) }
        }
        val (autoCandidates, manualCandidates) = candidates.partition { it.file.manualDownloadUrl == null }

        val job = globalScope.launch {
            val autoOk = recover({
                for (candidate in autoCandidates) installFile(candidate.projectId, candidate.file, instanceId)
                true
            }) { error: ProjectFileDownloadError ->
                snackbarService.error { error.message }
                false
            }

            val manualOk = autoOk && runManualDownloads(
                manualCandidates.map { candidate ->
                    ManualDownloadCandidate(
                        fileName = candidate.file.fileName,
                        downloadUrl = requireNotNull(candidate.file.manualDownloadUrl),
                        instanceId = instanceId,
                        projectType = projectType,
                        recordOrigin = {
                            recordInstalledContentOrigin(
                                projectType.toContentKind(),
                                instanceId,
                                candidate.file.fileName,
                                candidate.projectId.platform,
                                candidate.projectId.id,
                                candidate.file.id
                            )
                        }
                    )
                },
                awaitManualDownload, snackbarService, onManualDownloadItemsChanged
            )

            onFinished(autoOk && manualOk)
        }
        job.invokeOnCompletion { error -> if (error != null) onFinished(false) }
        return job
    }

    /**
     * Downloads [file] and records its provider origin as one unit — every install path (the primary file, each
     * confirmed dependency) must go through this rather than calling [downloadProjectFile] directly, so it's never
     * possible to download a project file without also recording where it came from.
     */
    context(_: Raise<ProjectFileDownloadError>)
    private suspend fun installFile(fileProjectId: ProjectId, file: ProjectFileView, instanceId: InstanceId) {
        downloadProjectFile(fileProjectId, file.id, file.fileName, instanceId, projectType)
        recordInstalledContentOrigin(
            projectType.toContentKind(),
            instanceId,
            file.fileName,
            fileProjectId.platform,
            fileProjectId.id,
            file.id
        )
    }

    @Composable
    private fun observeInstanceListView(): State<ProgressiveOutcome<Nothing, List<InstallableInstanceView>>> =
        produceAndCollectAsProgressiveState {
            observeInstanceList().map { res ->
                res.map { list ->
                    list.filterIsRight()
                        .mapNotNull { it.instance.toInstallableInstanceViewOrNull(projectType, compatibleLoaders) }
                }
            }
        }

    @Composable
    private fun observeInstalledProvidersView(
        instanceId: InstanceId?
    ): State<ProgressiveOutcome<Nothing, List<ModProvider>>> =
        produceAndCollectAsProgressiveState(instanceId) {
            if (instanceId == null) {
                flowOf(ProgressiveOutcome.Incomplete())
            } else {
                observeInstalledContent(projectType.toContentKind(), instanceId).map { outcome ->
                    outcome.map { list -> list.flatMap { it.providers } }
                }
            }
        }
}

private data class InstallCandidate(val projectId: ProjectId, val file: ProjectFileView)

private sealed interface DependencyResolution {
    data object NotApplicable : DependencyResolution
    data object Pending : DependencyResolution
    data class Resolved(val dependencies: List<DependencyFileView>) : DependencyResolution
}

internal fun pickRecommendedFile(files: List<ProjectFileView>): ProjectFileView? =
    files.filter { it.releaseChannel == ProjectReleaseChannel.Release }.maxByOrNull { it.publishedAt }
        ?: files.maxByOrNull { it.publishedAt }

/** Install is only wired up for these three types — see [app.vercors.feature.project.ui.details.isInstallable]. */
private fun ProjectType.toContentKind(): ContentKind = when (this) {
    ProjectType.Mod -> ContentKind.Mod
    ProjectType.ResourcePack -> ContentKind.ResourcePack
    ProjectType.ShaderPack -> ContentKind.ShaderPack
    ProjectType.Modpack, ProjectType.DataPack, ProjectType.World ->
        error("Install is not supported for project type $this")
}
