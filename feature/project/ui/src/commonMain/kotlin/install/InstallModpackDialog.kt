package app.vercors.feature.project.ui.install

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.usecase.GetModpackManifestUseCase
import app.vercors.feature.project.domain.usecase.GetProjectFilesUseCase
import app.vercors.feature.project.domain.usecase.GetProjectUseCase
import app.vercors.feature.project.domain.usecase.RunModpackInstallUseCase
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
    private val getProject: GetProjectUseCase,
    private val getModpackManifest: GetModpackManifestUseCase,
    private val createInstance: CreateInstanceUseCase,
    private val setInstanceIconFromUrl: SetInstanceIconFromUrlUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val runModpackInstall: RunModpackInstallUseCase,
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
                getProject = getProject,
                getModpackManifest = getModpackManifest,
                createInstance = createInstance,
                setInstanceIconFromUrl = setInstanceIconFromUrl,
                deleteInstance = deleteInstance,
                updateInstanceStatus = updateInstanceStatus,
                updateInstanceJob = updateInstanceJob,
                runModpackInstall = runModpackInstall,
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
