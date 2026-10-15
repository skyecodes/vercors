package app.vercors.feature.project.ui.install

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

sealed interface InstallModpackResult {
    data class Installed(val instanceId: InstanceId) : InstallModpackResult
    data object Cancelled : InstallModpackResult
}

@AssistedInject
class InstallModpackDialog(
    @Assisted private val projectId: ProjectId,
    @Assisted private val projectName: String,
    private val presenterFactory: InstallModpackPresenter.Factory,
) : DialogOverlay<InstallModpackResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<InstallModpackResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            presenterFactory.create(
                navigator = navigator,
                projectId = projectId,
                projectName = projectName,
            )
        }
        InstallModpack(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(projectId: ProjectId, projectName: String): InstallModpackDialog
    }
}
