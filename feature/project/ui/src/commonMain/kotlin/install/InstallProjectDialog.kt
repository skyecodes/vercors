package app.vercors.feature.project.ui.install

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

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
    private val presenterFactory: InstallProjectPresenter.Factory,
) : DialogOverlay<InstallProjectResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<InstallProjectResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            presenterFactory.create(
                navigator = navigator,
                projectId = projectId,
                projectType = projectType,
                compatibleLoaders = compatibleLoaders,
                preferredInstanceId = preferredInstanceId,
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
