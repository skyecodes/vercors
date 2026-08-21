package app.vercors.feature.project.ui.install

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.usecase.*
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
    private val resolveModpackManifestFiles: ResolveModpackManifestFilesUseCase,
    private val createInstance: CreateInstanceUseCase,
    private val installModpackFiles: InstallModpackFilesUseCase,
    private val awaitManualDownload: AwaitManualDownloadUseCase,
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val deleteInstanceFiles: DeleteInstanceFilesUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val updateManualDownloadItems: UpdateManualDownloadItemsUseCase,
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
                resolveModpackManifestFiles = resolveModpackManifestFiles,
                createInstance = createInstance,
                installModpackFiles = installModpackFiles,
                awaitManualDownload = awaitManualDownload,
                recordInstalledContentOrigin = recordInstalledContentOrigin,
                deleteInstance = deleteInstance,
                deleteInstanceFiles = deleteInstanceFiles,
                updateInstanceStatus = updateInstanceStatus,
                updateInstanceJob = updateInstanceJob,
                updateManualDownloadItems = updateManualDownloadItems,
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
