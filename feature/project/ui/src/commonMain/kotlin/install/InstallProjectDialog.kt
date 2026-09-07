package app.vercors.feature.project.ui.install

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.usecase.ObserveAvailableLoadersUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.domain.usecase.ObserveLoaderVersionsUseCase
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.usecase.*
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope

sealed interface InstallProjectResult {
    data class Installed(val instanceId: InstanceId) : InstallProjectResult
    data class GoToInstance(val instanceId: InstanceId) : InstallProjectResult
    data object Cancelled : InstallProjectResult
}

@AssistedInject
class InstallProjectDialog(
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
    private val setInstanceIconFromUrl: SetInstanceIconFromUrlUseCase,
    private val downloadProjectFiles: DownloadProjectFilesUseCase,
    private val awaitManualDownload: AwaitManualDownloadUseCase,
    private val installWorldFile: InstallWorldFileUseCase,
    private val awaitManualWorldDownload: AwaitManualWorldDownloadUseCase,
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val snackbarService: SnackbarService
) : DialogOverlay<InstallProjectResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<InstallProjectResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            InstallProjectPresenter(
                navigator = navigator,
                projectId = projectId,
                projectType = projectType,
                compatibleLoaders = compatibleLoaders,
                preferredInstanceId = preferredInstanceId,
                globalScope = globalScope,
                observeInstanceList = observeInstanceList,
                observeInstalledContent = observeInstalledContent,
                observeGameVersionList = observeGameVersionList,
                observeAvailableLoaders = observeAvailableLoaders,
                observeLoaderVersions = observeLoaderVersions,
                getProject = getProject,
                getProjectFiles = getProjectFiles,
                createInstance = createInstance,
                setInstanceIconFromUrl = setInstanceIconFromUrl,
                downloadProjectFiles = downloadProjectFiles,
                awaitManualDownload = awaitManualDownload,
                installWorldFile = installWorldFile,
                awaitManualWorldDownload = awaitManualWorldDownload,
                recordInstalledContentOrigin = recordInstalledContentOrigin,
                copyToClipboard = copyToClipboard,
                snackbarService = snackbarService
            )
        }
        InstallProject(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            projectId: ProjectId,
            projectType: ProjectType,
            compatibleLoaders: List<ModLoaderType>,
            preferredInstanceId: InstanceId?
        ): InstallProjectDialog
    }
}
