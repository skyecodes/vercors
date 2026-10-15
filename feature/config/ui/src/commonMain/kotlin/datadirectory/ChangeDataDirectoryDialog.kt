package app.vercors.feature.config.ui.datadirectory

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

// On success this never finishes: AppRestartService.restart ends the process
@AssistedInject
class ChangeDataDirectoryDialog(
    @Assisted private val currentPath: String,
    private val presenterFactory: ChangeDataDirectoryPresenter.Factory,
) : DialogOverlay<Unit>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<Unit>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            presenterFactory.create(currentPath = currentPath, navigator = navigator)
        }
        ChangeDataDirectory(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(currentPath: String): ChangeDataDirectoryDialog
    }
}
