package app.vercors.feature.project.ui.install

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.domain.usecase.RecordInstalledContentOriginUseCase
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.usecase.DownloadProjectFileUseCase
import app.vercors.feature.project.domain.usecase.GetProjectFilesUseCase
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
    data object Cancelled : InstallProjectResult
}

@AssistedInject
class InstallProjectDialog(
    @Assisted private val projectId: ProjectId,
    @Assisted private val projectType: ProjectType,
    @Assisted private val compatibleLoaders: List<ModLoaderType>,
    private val globalScope: CoroutineScope,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val getProjectFiles: GetProjectFilesUseCase,
    private val downloadProjectFile: DownloadProjectFileUseCase,
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase,
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
                globalScope = globalScope,
                observeInstanceList = observeInstanceList,
                getProjectFiles = getProjectFiles,
                downloadProjectFile = downloadProjectFile,
                recordInstalledContentOrigin = recordInstalledContentOrigin,
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
            compatibleLoaders: List<ModLoaderType>
        ): InstallProjectDialog
    }
}
