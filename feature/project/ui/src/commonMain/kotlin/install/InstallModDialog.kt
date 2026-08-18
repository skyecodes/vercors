package app.vercors.feature.project.ui.install

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.domain.usecase.RecordInstalledModOriginUseCase
import app.vercors.feature.project.domain.model.ProjectId
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

sealed interface InstallModResult {
    data class Installed(val instanceId: InstanceId) : InstallModResult
    data object Cancelled : InstallModResult
}

@AssistedInject
class InstallModDialog(
    @Assisted private val projectId: ProjectId,
    @Assisted private val compatibleLoaders: List<ModLoaderType>,
    private val globalScope: CoroutineScope,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val getProjectFiles: GetProjectFilesUseCase,
    private val downloadProjectFile: DownloadProjectFileUseCase,
    private val recordInstalledModOrigin: RecordInstalledModOriginUseCase,
    private val snackbarService: SnackbarService
) : DialogOverlay<InstallModResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<InstallModResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            InstallModPresenter(
                navigator = navigator,
                projectId = projectId,
                compatibleLoaders = compatibleLoaders,
                globalScope = globalScope,
                observeInstanceList = observeInstanceList,
                getProjectFiles = getProjectFiles,
                downloadProjectFile = downloadProjectFile,
                recordInstalledModOrigin = recordInstalledModOrigin,
                snackbarService = snackbarService
            )
        }
        InstallMod(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(projectId: ProjectId, compatibleLoaders: List<ModLoaderType>): InstallModDialog
    }
}
