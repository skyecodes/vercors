package app.vercors.feature.project.ui.install

import androidx.compose.runtime.*
import app.vercors.core.domain.filterIsRight
import app.vercors.core.ui.state.mutableProgressiveStateOf
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ContentKind
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.domain.usecase.RecordInstalledContentOriginUseCase
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectReleaseChannel
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.usecase.DownloadProjectFileUseCase
import app.vercors.feature.project.domain.usecase.GetProjectFilesUseCase
import app.vercors.feature.project.ui.message
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.recover
import com.slack.circuit.overlay.OverlayNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
    private val globalScope: CoroutineScope,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val getProjectFiles: GetProjectFilesUseCase,
    private val downloadProjectFile: DownloadProjectFileUseCase,
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase,
    private val snackbarService: SnackbarService
) : Presenter<InstallProjectState, InstallProjectEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<InstallProjectState, InstallProjectEvent> {
        val instances by observeInstanceListView()
        var selectedInstanceId by remember { mutableStateOf<InstanceId?>(null) }
        var selectedFileId by remember { mutableStateOf<String?>(null) }
        var installing by remember { mutableStateOf(false) }
        val files = remember { mutableProgressiveStateOf<ProjectDetailsError, List<ProjectFileView>>() }

        val selectedInstance = instances.valueOrNull?.find { it.id == selectedInstanceId }

        LaunchedEffect(selectedInstance?.id) {
            files.value = ProgressiveOutcome.Incomplete()
            selectedFileId = null
            if (selectedInstance != null) {
                val outcome = progressive {
                    getProjectFiles(
                        projectId,
                        selectedInstance.gameVersion,
                        if (projectType.requiresLoader) selectedInstance.loaderType else null
                    ).map { it.toView() }
                }
                files.value = outcome
                val fileList = outcome.valueOrNull
                if (fileList != null) {
                    val recommended = fileList.filter { it.releaseChannel == ProjectReleaseChannel.Release }
                        .maxByOrNull { it.publishedAt } ?: fileList.maxByOrNull { it.publishedAt }
                    selectedFileId = recommended?.id
                }
            }
        }

        return InstallProjectState(
            instances = instances,
            selectedInstanceId = selectedInstanceId,
            files = files.value,
            selectedFileId = selectedFileId,
            installing = installing
        ) + { event ->
            when (event) {
                is InstallProjectEvent.OnInstanceSelected -> selectedInstanceId = event.instanceId
                is InstallProjectEvent.OnFileSelected -> selectedFileId = event.fileId
                InstallProjectEvent.OnCancelClick -> navigator.finish(InstallProjectResult.Cancelled)
                InstallProjectEvent.OnConfirmClick -> {
                    val instanceId = selectedInstanceId
                    val file = files.value.valueOrNull?.find { it.id == selectedFileId }
                    if (instanceId != null && file != null) {
                        installing = true
                        globalScope.launch {
                            recover({
                                downloadProjectFile(projectId, file.id, file.fileName, instanceId, projectType)
                                recordInstalledContentOrigin(
                                    projectType.toContentKind(),
                                    instanceId,
                                    file.fileName,
                                    projectId.platform,
                                    projectId.id,
                                    file.id
                                )
                            }) { error -> snackbarService.error { error.message } }
                        }.invokeOnCompletion { error ->
                            installing = false
                            if (error == null) navigator.finish(InstallProjectResult.Installed(instanceId))
                        }
                    }
                }
            }
        }
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
}

/** Install is only wired up for these three types — see [app.vercors.feature.project.ui.details.isInstallable]. */
private fun ProjectType.toContentKind(): ContentKind = when (this) {
    ProjectType.Mod -> ContentKind.Mod
    ProjectType.ResourcePack -> ContentKind.ResourcePack
    ProjectType.ShaderPack -> ContentKind.ShaderPack
    ProjectType.Modpack, ProjectType.DataPack, ProjectType.World ->
        error("Install is not supported for project type $this")
}
