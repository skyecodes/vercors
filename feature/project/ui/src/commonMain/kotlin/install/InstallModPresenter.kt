package app.vercors.feature.project.ui.install

import androidx.compose.runtime.*
import app.vercors.core.domain.filterIsRight
import app.vercors.core.ui.state.mutableProgressiveStateOf
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.domain.usecase.RecordInstalledModOriginUseCase
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectReleaseChannel
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

class InstallModPresenter(
    private val navigator: OverlayNavigator<InstallModResult>,
    private val projectId: ProjectId,
    private val compatibleLoaders: List<ModLoaderType>,
    private val globalScope: CoroutineScope,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val getProjectFiles: GetProjectFilesUseCase,
    private val downloadProjectFile: DownloadProjectFileUseCase,
    private val recordInstalledModOrigin: RecordInstalledModOriginUseCase,
    private val snackbarService: SnackbarService
) : Presenter<InstallModState, InstallModEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<InstallModState, InstallModEvent> {
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
                    getProjectFiles(projectId, selectedInstance.gameVersion, selectedInstance.loaderType)
                        .map { it.toView() }
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

        return InstallModState(
            instances = instances,
            selectedInstanceId = selectedInstanceId,
            files = files.value,
            selectedFileId = selectedFileId,
            installing = installing
        ) + { event ->
            when (event) {
                is InstallModEvent.OnInstanceSelected -> selectedInstanceId = event.instanceId
                is InstallModEvent.OnFileSelected -> selectedFileId = event.fileId
                InstallModEvent.OnCancelClick -> navigator.finish(InstallModResult.Cancelled)
                InstallModEvent.OnConfirmClick -> {
                    val instanceId = selectedInstanceId
                    val file = files.value.valueOrNull?.find { it.id == selectedFileId }
                    if (instanceId != null && file != null) {
                        installing = true
                        globalScope.launch {
                            recover({
                                downloadProjectFile(projectId, file.id, file.fileName, instanceId)
                                recordInstalledModOrigin(
                                    instanceId,
                                    file.fileName,
                                    projectId.platform,
                                    projectId.id,
                                    file.id
                                )
                            }) { error -> snackbarService.error { error.message } }
                        }.invokeOnCompletion { error ->
                            installing = false
                            if (error == null) navigator.finish(InstallModResult.Installed(instanceId))
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
                    list.filterIsRight().mapNotNull { it.instance.toInstallableInstanceViewOrNull(compatibleLoaders) }
                }
            }
        }
}
