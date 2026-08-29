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
import app.vercors.feature.instance.ui.install.ManualDownloadCandidate
import app.vercors.feature.instance.ui.install.runManualDownloads
import app.vercors.feature.instance.ui.install.toFractionOrNull
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.usecase.AwaitManualDownloadUseCase
import app.vercors.feature.project.domain.usecase.DownloadProjectFilesUseCase
import app.vercors.feature.project.domain.usecase.GetProjectFilesUseCase
import app.vercors.feature.project.domain.usecase.GetProjectUseCase
import app.vercors.feature.project.ui.message
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
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
    private val downloadProjectFiles: DownloadProjectFilesUseCase,
    private val awaitManualDownload: AwaitManualDownloadUseCase,
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val snackbarService: SnackbarService
) : Presenter<InstallProjectState, InstallProjectEvent> {
    @Composable
    override fun present(): Store<InstallProjectState, InstallProjectEvent> {
        val instances by observeInstanceListView()
        val (selectedInstanceId, setSelectedInstanceId) = remember { mutableStateOf(preferredInstanceId) }
        val (selectedFileId, setSelectedFileId) = remember { mutableStateOf<String?>(null) }
        var dependencies by remember { mutableStateOf<List<DependencyFileView>>(emptyList()) }
        val (selectedDependencyIds, setSelectedDependencyIds) = remember { mutableStateOf<Set<ProjectId>>(emptySet()) }
        var resolvingDependencies by remember { mutableStateOf(false) }
        val (installing, setInstalling) = remember { mutableStateOf(false) }
        val (installProgress, setInstallProgress) = remember { mutableFloatStateOf(0f) }
        val (installJob, setInstallJob) = remember { mutableStateOf<Job?>(null) }
        val (manualDownloadItems, setManualDownloadItems) =
            remember { mutableStateOf<List<ManualDownloadItem>>(emptyList()) }
        val (completedInstanceId, setCompletedInstanceId) = remember { mutableStateOf<InstanceId?>(null) }
        val files = remember { mutableProgressiveStateOf<ProjectDetailsError, List<ProjectFileView>>() }

        val selectedInstance = instances.valueOrNull?.find { it.id == selectedInstanceId }
        val selectedFile = files.value.valueOrNull?.find { it.id == selectedFileId }
        val installedProviders by observeInstalledProvidersView(selectedInstanceId)

        LaunchedEffect(selectedInstance?.id) {
            setSelectedFileId(loadFiles(selectedInstance, files))
        }

        LaunchedEffect(selectedFileId, installedProviders) {
            when (val resolution = resolveDependencies(selectedFile, selectedInstance, installedProviders)) {
                DependencyResolution.NotApplicable -> {
                    dependencies = emptyList()
                    setSelectedDependencyIds(emptySet())
                    resolvingDependencies = false
                }

                DependencyResolution.Pending -> resolvingDependencies = true

                is DependencyResolution.Resolved -> {
                    dependencies = resolution.dependencies
                    setSelectedDependencyIds(resolution.dependencies.map { it.projectId }.toSet())
                    resolvingDependencies = false
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
            installProgress = installProgress,
            manualDownloadItems = manualDownloadItems,
            completedInstanceId = completedInstanceId
        ) + { event ->
            handleEvent(
                event = event,
                files = files,
                selectedInstanceId = selectedInstanceId,
                setSelectedInstanceId = setSelectedInstanceId,
                selectedFileId = selectedFileId,
                setSelectedFileId = setSelectedFileId,
                dependencies = dependencies,
                selectedDependencyIds = selectedDependencyIds,
                setSelectedDependencyIds = setSelectedDependencyIds,
                installJob = installJob,
                setInstallJob = setInstallJob,
                setInstalling = setInstalling,
                setInstallProgress = setInstallProgress,
                setManualDownloadItems = setManualDownloadItems,
                completedInstanceId = completedInstanceId,
                setCompletedInstanceId = setCompletedInstanceId,
            )
        }
    }

    private fun handleEvent(
        event: InstallProjectEvent,
        files: MutableState<ProgressiveOutcome<ProjectDetailsError, List<ProjectFileView>>>,
        selectedInstanceId: InstanceId?,
        setSelectedInstanceId: (InstanceId?) -> Unit,
        selectedFileId: String?,
        setSelectedFileId: (String?) -> Unit,
        dependencies: List<DependencyFileView>,
        selectedDependencyIds: Set<ProjectId>,
        setSelectedDependencyIds: (Set<ProjectId>) -> Unit,
        installJob: Job?,
        setInstallJob: (Job?) -> Unit,
        setInstalling: (Boolean) -> Unit,
        setInstallProgress: (Float) -> Unit,
        setManualDownloadItems: (List<ManualDownloadItem>) -> Unit,
        completedInstanceId: InstanceId?,
        setCompletedInstanceId: (InstanceId?) -> Unit,
    ) {
        when (event) {
            is InstallProjectEvent.OnInstanceSelected -> setSelectedInstanceId(event.instanceId)
            is InstallProjectEvent.OnFileSelected -> setSelectedFileId(event.fileId)
            is InstallProjectEvent.OnDependencyToggled -> setSelectedDependencyIds(
                if (event.checked) selectedDependencyIds + event.projectId
                else selectedDependencyIds - event.projectId
            )

            is InstallProjectEvent.OnCopyUrlClick -> copyToClipboard(event.url)

            InstallProjectEvent.OnCancelClick -> {
                if (installJob != null && installJob.isActive) installJob.cancel()
                else navigator.finish(InstallProjectResult.Cancelled)
            }

            InstallProjectEvent.OnConfirmClick -> handleConfirmClick(
                files, selectedInstanceId, selectedFileId, dependencies, selectedDependencyIds,
                setInstalling, setInstallProgress, setInstallJob, setManualDownloadItems, setCompletedInstanceId
            )

            InstallProjectEvent.OnCloseClick ->
                navigator.finish(InstallProjectResult.Installed(requireNotNull(completedInstanceId)))

            InstallProjectEvent.OnGoToInstanceClick ->
                navigator.finish(InstallProjectResult.GoToInstance(requireNotNull(completedInstanceId)))
        }
    }

    private fun handleConfirmClick(
        files: MutableState<ProgressiveOutcome<ProjectDetailsError, List<ProjectFileView>>>,
        selectedInstanceId: InstanceId?,
        selectedFileId: String?,
        dependencies: List<DependencyFileView>,
        selectedDependencyIds: Set<ProjectId>,
        setInstalling: (Boolean) -> Unit,
        setInstallProgress: (Float) -> Unit,
        setInstallJob: (Job?) -> Unit,
        setManualDownloadItems: (List<ManualDownloadItem>) -> Unit,
        setCompletedInstanceId: (InstanceId?) -> Unit,
    ) {
        val instanceId = selectedInstanceId
        val file = files.value.valueOrNull?.find { it.id == selectedFileId }
        if (instanceId != null && file != null) {
            val selectedDependencies = dependencies.filter { it.projectId in selectedDependencyIds }
            setInstalling(true)
            setInstallProgress(0f)
            setInstallJob(
                confirmInstall(
                    instanceId, file, selectedDependencies,
                    onManualDownloadItemsChanged = { setManualDownloadItems(it) },
                    onProgressChanged = { setInstallProgress(it) }
                ) { success ->
                    setInstalling(false)
                    setManualDownloadItems(emptyList())
                    if (success) setCompletedInstanceId(instanceId)
                }
            )
        }
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

    /**
     * Installs [file] (and each of [selectedDependencies]), auto files first (as one batch download, so their
     * progress can be merged into a single fraction), then manual ones. [onProgressChanged] reports the overall
     * fraction (0..1) across every candidate — auto candidates count as done once the batch download finishes,
     * manual candidates count as done one by one as [runManualDownloads] reports each item downloaded.
     */
    private fun confirmInstall(
        instanceId: InstanceId,
        file: ProjectFileView,
        selectedDependencies: List<DependencyFileView>,
        onManualDownloadItemsChanged: (List<ManualDownloadItem>) -> Unit,
        onProgressChanged: (Float) -> Unit,
        onFinished: (success: Boolean) -> Unit
    ): Job {
        val candidates = buildList {
            add(InstallCandidate(projectId, file))
            selectedDependencies.forEach { add(InstallCandidate(it.projectId, it.file)) }
        }
        val (autoCandidates, manualCandidates) = candidates.partition { it.file.manualDownloadUrl == null }
        val totalCount = candidates.size

        val job = globalScope.launch {
            val autoOk = recover({
                if (autoCandidates.isNotEmpty()) {
                    downloadProjectFiles(
                        autoCandidates.map {
                            ProjectFileDownloadCandidate(
                                it.projectId, it.file.id, it.file.fileName, it.file.size, it.file.sha1, it.file.md5
                            )
                        },
                        instanceId, projectType
                    ) { progress ->
                        onProgressChanged((progress.toFractionOrNull() ?: 0f) * autoCandidates.size / totalCount)
                    }
                    autoCandidates.forEach { candidate ->
                        recordInstalledContentOrigin(
                            projectType.toContentKind(),
                            instanceId,
                            candidate.file.fileName,
                            candidate.projectId.platform,
                            candidate.projectId.id,
                            candidate.file.id
                        )
                    }
                }
                onProgressChanged(autoCandidates.size.toFloat() / totalCount)
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
                        size = candidate.file.size,
                        sha1 = candidate.file.sha1,
                        md5 = candidate.file.md5,
                        platformId = candidate.projectId.platform,
                        providerProjectId = candidate.projectId.id,
                        providerFileId = candidate.file.id,
                    )
                },
                awaitManualDownload, recordInstalledContentOrigin, snackbarService
            ) { items ->
                onManualDownloadItemsChanged(items)
                onProgressChanged((autoCandidates.size + items.count { it.downloaded }).toFloat() / totalCount)
            }

            onFinished(autoOk && manualOk)
        }
        job.invokeOnCompletion { error -> if (error != null) onFinished(false) }
        return job
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
