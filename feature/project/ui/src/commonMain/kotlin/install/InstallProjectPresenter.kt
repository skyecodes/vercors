package app.vercors.feature.project.ui.install

import androidx.compose.runtime.*
import app.vercors.core.domain.filterIsRight
import app.vercors.core.ui.state.mutableProgressiveStateOf
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.usecase.ObserveAvailableLoadersUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.domain.usecase.ObserveLoaderVersionsUseCase
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstalledContentUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.ui.observeAvailableLoadersView
import app.vercors.feature.instance.ui.observeGameVersionListView
import app.vercors.feature.instance.ui.observeLoaderVersionsView
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.usecase.ApplyProjectIconUseCase
import app.vercors.feature.project.domain.usecase.GetProjectFilesUseCase
import app.vercors.feature.project.domain.usecase.GetProjectUseCase
import app.vercors.feature.project.domain.usecase.InstallProjectFilesUseCase
import app.vercors.feature.project.ui.message
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.recover
import com.slack.circuit.overlay.OverlayNavigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull
import app.vercors.feature.instance.ui.message as manualDownloadMessage

@AssistedInject
class InstallProjectPresenter(
    @Assisted private val navigator: OverlayNavigator<InstallProjectResult>,
    @Assisted private val projectId: ProjectId,
    @Assisted private val projectType: ProjectType,
    @Assisted private val compatibleLoaders: List<ModLoaderType>,
    @Assisted private val preferredInstanceId: InstanceId?,
    private val globalScope: CoroutineScope,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val observeInstalledContent: ObserveInstalledContentUseCase,
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val observeAvailableLoaders: ObserveAvailableLoadersUseCase,
    private val observeLoaderVersions: ObserveLoaderVersionsUseCase,
    private val getProject: GetProjectUseCase,
    private val getProjectFiles: GetProjectFilesUseCase,
    private val createInstance: CreateInstanceUseCase,
    private val applyProjectIcon: ApplyProjectIconUseCase,
    private val installProjectFiles: InstallProjectFilesUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val snackbarService: SnackbarService
) : Presenter<InstallProjectState, InstallProjectEvent> {
    @Composable
    override fun present(): Store<InstallProjectState, InstallProjectEvent> {
        val instances by observeInstanceListView()
        val instanceSelection = rememberInstanceSelectionState(instances)
        val newInstanceForm = rememberNewInstanceFormState()

        val selectedInstance = instances.valueOrNull?.find { it.id == instanceSelection.selectedInstanceId }
        val effectiveGameVersion = if (instanceSelection.creatingNewInstance) {
            newInstanceForm.selectedGameVersion?.id
        } else {
            selectedInstance?.gameVersion
        }
        val effectiveLoader = if (instanceSelection.creatingNewInstance) {
            newInstanceForm.selectedLoader
        } else {
            selectedInstance?.loaderType
        }
        val fileSelection =
            rememberFileSelectionState(effectiveGameVersion, effectiveLoader, instanceSelection.selectedInstanceId)

        val (installing, setInstalling) = remember { mutableStateOf(false) }
        val (installProgress, setInstallProgress) = remember { mutableStateOf(InstallProgressView()) }
        val (installJob, setInstallJob) = remember { mutableStateOf<Job?>(null) }
        val (manualDownloadItems, setManualDownloadItems) =
            remember { mutableStateOf<List<ManualDownloadItem>>(emptyList()) }
        val (completedInstanceId, setCompletedInstanceId) = remember { mutableStateOf<InstanceId?>(null) }

        return InstallProjectState(
            projectType = projectType,
            instances = instances,
            selectedInstanceId = instanceSelection.selectedInstanceId,
            creatingNewInstance = instanceSelection.creatingNewInstance,
            newInstanceName = newInstanceForm.newInstanceName,
            project = newInstanceForm.project,
            newInstanceGameVersionList = newInstanceForm.gameVersionList,
            selectedNewGameVersion = newInstanceForm.selectedGameVersion,
            newInstanceLoaderList = newInstanceForm.loaderList,
            selectedNewLoader = newInstanceForm.selectedLoader,
            newInstanceLoaderVersionList = newInstanceForm.loaderVersionList,
            selectedNewLoaderVersion = newInstanceForm.selectedLoaderVersion,
            files = fileSelection.files.value,
            selectedFileId = fileSelection.selectedFileId,
            dependencies = fileSelection.dependencies,
            selectedDependencyIds = fileSelection.selectedDependencyIds,
            resolvingDependencies = fileSelection.resolvingDependencies,
            installing = installing,
            installProgress = installProgress,
            manualDownloadItems = manualDownloadItems,
            completedInstanceId = completedInstanceId
        ) + { event ->
            handleEvent(
                event = event,
                instanceSelection = instanceSelection,
                newInstanceForm = newInstanceForm,
                fileSelection = fileSelection,
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

    @Composable
    private fun rememberInstanceSelectionState(
        instances: ProgressiveOutcome<Nothing, List<InstallableInstanceView>>
    ): InstanceSelectionState {
        val (selectedInstanceId, setSelectedInstanceId) = remember { mutableStateOf(preferredInstanceId) }
        val (creatingNewInstance, setCreatingNewInstance) = remember { mutableStateOf(false) }
        val (hasAppliedDefaultSelection, setHasAppliedDefaultSelection) = remember { mutableStateOf(false) }

        LaunchedEffect(instances.valueOrNull) {
            if (hasAppliedDefaultSelection) return@LaunchedEffect
            val list = instances.valueOrNull ?: return@LaunchedEffect
            setHasAppliedDefaultSelection(true)
            val preferred = preferredInstanceId?.let { id -> list.find { it.id == id } }
            val default = preferred ?: list.firstOrNull()
            if (default != null) setSelectedInstanceId(default.id) else setCreatingNewInstance(true)
        }

        return InstanceSelectionState(
            selectedInstanceId = selectedInstanceId,
            setSelectedInstanceId = setSelectedInstanceId,
            creatingNewInstance = creatingNewInstance,
            setCreatingNewInstance = setCreatingNewInstance,
        )
    }

    @Composable
    private fun rememberNewInstanceFormState(): NewInstanceFormState {
        val project = remember { mutableProgressiveStateOf<ProjectDetailsError, Project>() }
        val (newInstanceName, setNewInstanceName) = remember { mutableStateOf("") }
        val (hasEditedNewInstanceName, setHasEditedNewInstanceName) = remember { mutableStateOf(false) }
        val (selectedGameVersion, setSelectedGameVersion) = remember { mutableStateOf<GameVersionView?>(null) }
        val (selectedLoader, setSelectedLoader) = remember { mutableStateOf<ModLoaderType?>(null) }
        val (selectedLoaderVersion, setSelectedLoaderVersion) = remember { mutableStateOf<LoaderVersion?>(null) }

        val gameVersionList by observeGameVersionListView(observeGameVersionList)
        val availableLoaders by observeAvailableLoadersView(observeAvailableLoaders, selectedGameVersion?.id)
        val loaderVersionList by observeLoaderVersionsView(
            observeLoaderVersions, selectedGameVersion?.id, selectedLoader
        )
        val filteredGameVersionList = remember(gameVersionList, project.value) {
            val supportedVersions = project.value.valueOrNull?.gameVersions
            val allVersions = gameVersionList.valueOrNull.orEmpty()
            if (supportedVersions == null) allVersions else allVersions.filter { it.id in supportedVersions }
        }
        // Per-version files: compatibleLoaders aggregates every version and can list loaders with no file for this one
        val versionFiles = remember { mutableProgressiveStateOf<ProjectDetailsError, List<ProjectFileView>>() }
        val loaderList = remember(availableLoaders, versionFiles.value) {
            if (!projectType.requiresLoader) {
                availableLoaders.valueOrNull.orEmpty()
            } else {
                versionFiles.value.valueOrNull
                    ?.flatMap { it.loaders }
                    ?.distinct()
                    ?.filter { it in compatibleLoaders }
                    ?: emptyList()
            }
        }

        LaunchedEffect(projectId) {
            project.value = loadProject()
        }

        LaunchedEffect(selectedGameVersion?.id) {
            val gameVersion = selectedGameVersion?.id
            if (!projectType.requiresLoader || gameVersion == null) {
                versionFiles.value = ProgressiveOutcome.Incomplete()
                return@LaunchedEffect
            }
            versionFiles.value = ProgressiveOutcome.Incomplete()
            versionFiles.value = loadVersionFiles(gameVersion)
        }

        LaunchedEffect(project.value) {
            if (!hasEditedNewInstanceName) {
                project.value.valueOrNull?.name?.let { setNewInstanceName(it) }
            }
        }

        LaunchedEffect(loaderList) {
            if (selectedLoader == null) {
                setSelectedLoader(loaderList.firstOrNull())
            }
        }

        LaunchedEffect(loaderVersionList) {
            if (selectedLoaderVersion == null) {
                setSelectedLoaderVersion(
                    loaderVersionList.valueOrNull?.let { versions ->
                        versions.firstOrNull { it.stable } ?: versions.firstOrNull()
                    }
                )
            }
        }

        return NewInstanceFormState(
            project = project.value,
            newInstanceName = newInstanceName,
            setNewInstanceName = setNewInstanceName,
            setHasEditedNewInstanceName = setHasEditedNewInstanceName,
            gameVersionList = filteredGameVersionList,
            selectedGameVersion = selectedGameVersion,
            setSelectedGameVersion = setSelectedGameVersion,
            loaderList = loaderList,
            selectedLoader = selectedLoader,
            setSelectedLoader = setSelectedLoader,
            loaderVersionList = loaderVersionList,
            selectedLoaderVersion = selectedLoaderVersion,
            setSelectedLoaderVersion = setSelectedLoaderVersion,
        )
    }

    @Composable
    private fun rememberFileSelectionState(
        gameVersion: String?,
        loader: ModLoaderType?,
        instanceId: InstanceId?,
    ): FileSelectionState {
        val (selectedFileId, setSelectedFileId) = remember { mutableStateOf<String?>(null) }
        val (dependencies, setDependencies) = remember { mutableStateOf<List<DependencyFileView>>(emptyList()) }
        val (selectedDependencyIds, setSelectedDependencyIds) = remember { mutableStateOf<Set<ProjectId>>(emptySet()) }
        val (resolvingDependencies, setResolvingDependencies) = remember { mutableStateOf(false) }
        val files = remember { mutableProgressiveStateOf<ProjectDetailsError, List<ProjectFileView>>() }
        val installedProviders by observeInstalledProvidersView(instanceId)
        val selectedFile = files.value.valueOrNull?.find { it.id == selectedFileId }

        LaunchedEffect(gameVersion, loader) {
            setSelectedFileId(loadFiles(gameVersion, loader, files))
        }

        LaunchedEffect(selectedFileId, installedProviders) {
            when (val resolution = resolveDependencies(selectedFile, gameVersion, loader, installedProviders)) {
                DependencyResolution.NotApplicable -> {
                    setDependencies(emptyList())
                    setSelectedDependencyIds(emptySet())
                    setResolvingDependencies(false)
                }

                DependencyResolution.Pending -> setResolvingDependencies(true)

                is DependencyResolution.Resolved -> {
                    setDependencies(resolution.dependencies)
                    setSelectedDependencyIds(resolution.dependencies.map { it.projectId }.toSet())
                    setResolvingDependencies(false)
                }
            }
        }

        return FileSelectionState(
            files = files,
            selectedFileId = selectedFileId,
            setSelectedFileId = setSelectedFileId,
            dependencies = dependencies,
            selectedDependencyIds = selectedDependencyIds,
            setSelectedDependencyIds = setSelectedDependencyIds,
            resolvingDependencies = resolvingDependencies,
        )
    }

    private fun handleEvent(
        event: InstallProjectEvent,
        instanceSelection: InstanceSelectionState,
        newInstanceForm: NewInstanceFormState,
        fileSelection: FileSelectionState,
        installJob: Job?,
        setInstallJob: (Job?) -> Unit,
        setInstalling: (Boolean) -> Unit,
        setInstallProgress: (InstallProgressView) -> Unit,
        setManualDownloadItems: (List<ManualDownloadItem>) -> Unit,
        completedInstanceId: InstanceId?,
        setCompletedInstanceId: (InstanceId?) -> Unit,
    ) {
        when (event) {
            is InstallProjectEvent.OnInstanceSelectorItemSelected -> when (val item = event.item) {
                is InstanceSelectorItem.Existing -> {
                    instanceSelection.setSelectedInstanceId(item.instance.id)
                    instanceSelection.setCreatingNewInstance(false)
                }

                InstanceSelectorItem.CreateNew -> {
                    instanceSelection.setSelectedInstanceId(null)
                    instanceSelection.setCreatingNewInstance(true)
                }

                null -> {
                    instanceSelection.setSelectedInstanceId(null)
                    instanceSelection.setCreatingNewInstance(false)
                }
            }

            is InstallProjectEvent.OnNewInstanceNameChange -> {
                newInstanceForm.setHasEditedNewInstanceName(true)
                newInstanceForm.setNewInstanceName(event.name)
            }

            is InstallProjectEvent.OnNewGameVersionChange -> {
                newInstanceForm.setSelectedGameVersion(event.version)
                newInstanceForm.setSelectedLoader(null)
                newInstanceForm.setSelectedLoaderVersion(null)
            }

            is InstallProjectEvent.OnNewLoaderChange -> {
                newInstanceForm.setSelectedLoader(event.loader)
                newInstanceForm.setSelectedLoaderVersion(null)
            }

            is InstallProjectEvent.OnNewLoaderVersionChange -> newInstanceForm.setSelectedLoaderVersion(event.version)

            is InstallProjectEvent.OnFileSelected -> fileSelection.setSelectedFileId(event.fileId)
            is InstallProjectEvent.OnDependencyToggled -> fileSelection.setSelectedDependencyIds(
                if (event.checked) fileSelection.selectedDependencyIds + event.projectId
                else fileSelection.selectedDependencyIds - event.projectId
            )

            is InstallProjectEvent.OnCopyUrlClick -> copyToClipboard(event.url)

            InstallProjectEvent.OnCancelClick -> {
                if (installJob != null && installJob.isActive) installJob.cancel()
                else navigator.finish(InstallProjectResult.Cancelled)
            }

            InstallProjectEvent.OnConfirmClick -> handleConfirmClick(
                instanceSelection = instanceSelection,
                newInstanceForm = newInstanceForm,
                fileSelection = fileSelection,
                setInstalling = setInstalling,
                setInstallProgress = setInstallProgress,
                setInstallJob = setInstallJob,
                setManualDownloadItems = setManualDownloadItems,
                setCompletedInstanceId = setCompletedInstanceId,
            )

            InstallProjectEvent.OnCloseClick ->
                navigator.finish(InstallProjectResult.Installed(requireNotNull(completedInstanceId)))

            InstallProjectEvent.OnGoToInstanceClick ->
                navigator.finish(InstallProjectResult.GoToInstance(requireNotNull(completedInstanceId)))
        }
    }

    private fun handleConfirmClick(
        instanceSelection: InstanceSelectionState,
        newInstanceForm: NewInstanceFormState,
        fileSelection: FileSelectionState,
        setInstalling: (Boolean) -> Unit,
        setInstallProgress: (InstallProgressView) -> Unit,
        setInstallJob: (Job?) -> Unit,
        setManualDownloadItems: (List<ManualDownloadItem>) -> Unit,
        setCompletedInstanceId: (InstanceId?) -> Unit,
    ) {
        val file = fileSelection.files.value.valueOrNull?.find { it.id == fileSelection.selectedFileId } ?: return
        val selectedDependencies =
            fileSelection.dependencies.filter { it.projectId in fileSelection.selectedDependencyIds }

        if (instanceSelection.creatingNewInstance) {
            val selectedGameVersion = newInstanceForm.selectedGameVersion ?: return
            setInstalling(true)
            setInstallProgress(InstallProgressView())
            setInstallJob(
                confirmCreateAndInstall(
                    newInstanceForm.newInstanceName, selectedGameVersion, newInstanceForm.selectedLoader,
                    newInstanceForm.selectedLoaderVersion, file, selectedDependencies,
                    onManualDownloadItemsChanged = { setManualDownloadItems(it) },
                    onProgressChanged = { setInstallProgress(it) },
                ) { instanceId, success ->
                    setInstalling(false)
                    setManualDownloadItems(emptyList())
                    if (success && instanceId != null) setCompletedInstanceId(instanceId)
                }
            )
        } else {
            val instanceId = instanceSelection.selectedInstanceId ?: return
            setInstalling(true)
            setInstallProgress(InstallProgressView())
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

    @OptIn(ExperimentalProgressiveRaiseApi::class)
    private suspend fun loadProject(): ProgressiveOutcome<ProjectDetailsError, Project> =
        progressive { getProject(projectId) }

    @OptIn(ExperimentalProgressiveRaiseApi::class)
    private suspend fun loadVersionFiles(gameVersion: String): ProgressiveOutcome<ProjectDetailsError, List<ProjectFileView>> =
        progressive { getProjectFiles(projectId, gameVersion, loader = null).map { it.toView() } }

    // Always fetched with loader = null: upstream loader filters are strict exact matches (e.g. Fabric files that run on Quilt)
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    private suspend fun loadFiles(
        gameVersion: String?,
        loader: ModLoaderType?,
        files: MutableState<ProgressiveOutcome<ProjectDetailsError, List<ProjectFileView>>>
    ): String? {
        files.value = ProgressiveOutcome.Incomplete()
        if (gameVersion == null) return null
        val outcome = progressive {
            getProjectFiles(
                projectId,
                gameVersion,
                if (projectType.requiresLoader) loader else null
            ).map { it.toView() }
        }
        files.value = outcome
        return outcome.valueOrNull?.let(::pickRecommendedFile)?.id
    }

    // Stays Pending while installed content loads, or an installed dependency could look missing and be re-downloaded
    private suspend fun resolveDependencies(
        file: ProjectFileView?,
        gameVersion: String?,
        loader: ModLoaderType?,
        installedProviders: ProgressiveOutcome<Nothing, List<ModProvider>>
    ): DependencyResolution {
        if (file == null || gameVersion == null || file.dependencies.isEmpty()) return DependencyResolution.NotApplicable
        if (installedProviders is ProgressiveOutcome.Incomplete) return DependencyResolution.Pending

        val installedKeys = installedProviders.valueOrNull.orEmpty().map { it.platformId to it.projectId }.toSet()
        val depsToResolve = file.dependencies.filterNot { (it.platform to it.id) in installedKeys }
        if (depsToResolve.isEmpty()) return DependencyResolution.Resolved(emptyList())

        val resolved = coroutineScope {
            depsToResolve.map { dependencyId ->
                async {
                    resolveDependency(
                        dependencyId,
                        gameVersion,
                        if (projectType.requiresLoader) loader else null
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
        val fileList = getProjectFiles(dependencyId, gameVersion, loader = null).map { it.toView() }
            .let { list -> if (loader != null) list.filter { loader in it.loaders } else list }
        pickRecommendedFile(fileList)?.let { DependencyFileView(dependencyId, project.name, project.type, it) }
    }) { null }

    private fun confirmInstall(
        instanceId: InstanceId,
        file: ProjectFileView,
        selectedDependencies: List<DependencyFileView>,
        onManualDownloadItemsChanged: (List<ManualDownloadItem>) -> Unit,
        onProgressChanged: (InstallProgressView) -> Unit,
        onFinished: (success: Boolean) -> Unit
    ): Job {
        val job = globalScope.launch {
            val ok =
                installFiles(instanceId, file, selectedDependencies, onManualDownloadItemsChanged, onProgressChanged)
            onFinished(ok)
        }
        job.invokeOnCompletion { error -> if (error != null) onFinished(false) }
        return job
    }

    // Uses the returned Instance directly: the reactive cache lags behind the write
    private fun confirmCreateAndInstall(
        newInstanceName: String,
        selectedNewGameVersion: GameVersionView,
        selectedNewLoader: ModLoaderType?,
        selectedNewLoaderVersion: LoaderVersion?,
        file: ProjectFileView,
        selectedDependencies: List<DependencyFileView>,
        onManualDownloadItemsChanged: (List<ManualDownloadItem>) -> Unit,
        onProgressChanged: (InstallProgressView) -> Unit,
        onFinished: (instanceId: InstanceId?, success: Boolean) -> Unit
    ): Job {
        val job = globalScope.launch {
            val instance = tryCreateInstance(
                Instance(
                    name = newInstanceName,
                    gameVersion = selectedNewGameVersion.id,
                    modLoader = selectedNewLoader?.let {
                        InstanceModLoader(type = it, version = selectedNewLoaderVersion!!.version)
                    }
                )
            )
            if (instance == null) {
                onFinished(null, false)
                return@launch
            }

            // Child of `job`, not `globalScope`, so a best-effort icon fetch never outlives a cancelled install.
            launch { applyProjectIcon(instance, projectId) }

            val ok =
                installFiles(instance.id, file, selectedDependencies, onManualDownloadItemsChanged, onProgressChanged)
            onFinished(instance.id, ok)
        }
        job.invokeOnCompletion { error -> if (error != null) onFinished(null, false) }
        return job
    }

    private suspend fun tryCreateInstance(instance: Instance): Instance? =
        recover({ createInstance(instance) }) { error: InstanceValidationError ->
            snackbarService.error { error.message }
            null
        }

    private suspend fun installFiles(
        instanceId: InstanceId,
        file: ProjectFileView,
        selectedDependencies: List<DependencyFileView>,
        onManualDownloadItemsChanged: (List<ManualDownloadItem>) -> Unit,
        onProgressChanged: (InstallProgressView) -> Unit,
    ): Boolean = installProjectFiles(
        instanceId = instanceId,
        project = file.toInstallCandidate(projectId, projectType),
        dependencies = selectedDependencies.map { it.file.toInstallCandidate(it.projectId, it.type) },
        onDownloadError = { error -> snackbarService.error { error.message } },
        onManualDownloadError = { error -> snackbarService.error { error.manualDownloadMessage } },
        onManualDownloadItemsChanged = onManualDownloadItemsChanged,
        onProgress = { onProgressChanged(InstallProgressView(it.fraction, it.download)) },
    )

    @Composable
    private fun observeInstanceListView(): State<ProgressiveOutcome<Nothing, List<InstallableInstanceView>>> =
        produceAndCollectAsProgressiveState {
            observeInstanceList().map { res ->
                res.map { list ->
                    list.filterIsRight()
                        .mapNotNull { it.instance.toInstallableInstanceViewOrNull(projectType, compatibleLoaders) }
                        .sortedByDescending { it.updatedAt }
                }
            }
        }

    @Composable
    private fun observeInstalledProvidersView(
        instanceId: InstanceId?
    ): State<ProgressiveOutcome<Nothing, List<ModProvider>>> =
        produceAndCollectAsProgressiveState(instanceId) {
            val kind = projectType.toContentKindOrNull()
            if (instanceId == null || kind == null) {
                flowOf(ProgressiveOutcome.Success(emptyList<ModProvider>()))
            } else {
                observeInstalledContent(kind, instanceId).map { outcome ->
                    outcome.map { list -> list.flatMap { it.providers } }
                }
            }
        }

    @AssistedFactory
    fun interface Factory {
        fun create(
            navigator: OverlayNavigator<InstallProjectResult>,
            projectId: ProjectId,
            projectType: ProjectType,
            compatibleLoaders: List<ModLoaderType>,
            preferredInstanceId: InstanceId?
        ): InstallProjectPresenter
    }
}

private data class InstanceSelectionState(
    val selectedInstanceId: InstanceId?,
    val setSelectedInstanceId: (InstanceId?) -> Unit,
    val creatingNewInstance: Boolean,
    val setCreatingNewInstance: (Boolean) -> Unit,
)

private data class NewInstanceFormState(
    val project: ProgressiveOutcome<ProjectDetailsError, Project>,
    val newInstanceName: String,
    val setNewInstanceName: (String) -> Unit,
    val setHasEditedNewInstanceName: (Boolean) -> Unit,
    val gameVersionList: List<GameVersionView>,
    val selectedGameVersion: GameVersionView?,
    val setSelectedGameVersion: (GameVersionView?) -> Unit,
    val loaderList: List<ModLoaderType>,
    val selectedLoader: ModLoaderType?,
    val setSelectedLoader: (ModLoaderType?) -> Unit,
    val loaderVersionList: ProgressiveOutcome<LoaderError, List<LoaderVersion>>,
    val selectedLoaderVersion: LoaderVersion?,
    val setSelectedLoaderVersion: (LoaderVersion?) -> Unit,
)

private data class FileSelectionState(
    val files: MutableState<ProgressiveOutcome<ProjectDetailsError, List<ProjectFileView>>>,
    val selectedFileId: String?,
    val setSelectedFileId: (String?) -> Unit,
    val dependencies: List<DependencyFileView>,
    val selectedDependencyIds: Set<ProjectId>,
    val setSelectedDependencyIds: (Set<ProjectId>) -> Unit,
    val resolvingDependencies: Boolean,
)

private fun ProjectFileView.toInstallCandidate(projectId: ProjectId, type: ProjectType): ProjectInstallCandidate =
    ProjectInstallCandidate(projectId, type, id, fileName, size, sha1, md5, manualDownloadUrl)

private sealed interface DependencyResolution {
    data object NotApplicable : DependencyResolution
    data object Pending : DependencyResolution
    data class Resolved(val dependencies: List<DependencyFileView>) : DependencyResolution
}

internal fun pickRecommendedFile(files: List<ProjectFileView>): ProjectFileView? =
    files.filter { it.releaseChannel == ProjectReleaseChannel.Release }.maxByOrNull { it.publishedAt }
        ?: files.maxByOrNull { it.publishedAt }
