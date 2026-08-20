package app.vercors.feature.project.ui.install

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.feature.instance.domain.usecase.DeleteInstanceFilesUseCase
import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.feature.instance.domain.usecase.RecordInstalledContentOriginUseCase
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.usecase.AwaitManualDownloadUseCase
import app.vercors.feature.project.domain.usecase.GetModpackManifestUseCase
import app.vercors.feature.project.domain.usecase.GetProjectFilesUseCase
import app.vercors.feature.project.domain.usecase.InstallModpackFilesUseCase
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope

sealed interface InstallModpackResult {
    data class Installed(val instanceId: InstanceId) : InstallModpackResult
    data object Cancelled : InstallModpackResult
}

@AssistedInject
class InstallModpackDialog(
    @Assisted private val projectId: ProjectId,
    @Assisted private val projectName: String,
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
) : DialogOverlay<InstallModpackResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<InstallModpackResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            InstallModpackPresenter(
                navigator = navigator,
                projectId = projectId,
                projectName = projectName,
                globalScope = globalScope,
                getProjectFiles = getProjectFiles,
                getModpackManifest = getModpackManifest,
                createInstance = createInstance,
                installModpackFiles = installModpackFiles,
                awaitManualDownload = awaitManualDownload,
                recordInstalledContentOrigin = recordInstalledContentOrigin,
                deleteInstance = deleteInstance,
                deleteInstanceFiles = deleteInstanceFiles,
                copyToClipboard = copyToClipboard,
                snackbarService = snackbarService
            )
        }
        InstallModpack(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(projectId: ProjectId, projectName: String): InstallModpackDialog
    }
}
