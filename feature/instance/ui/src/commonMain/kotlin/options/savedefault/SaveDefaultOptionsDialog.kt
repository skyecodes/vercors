package app.vercors.feature.instance.ui.options.savedefault

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

sealed interface SaveDefaultOptionsResult {
    data object Saved : SaveDefaultOptionsResult
    data object Cancelled : SaveDefaultOptionsResult
}

@AssistedInject
class SaveDefaultOptionsDialog(
    @Assisted private val target: SaveDefaultOptionsTarget,
    private val presenterFactory: SaveDefaultOptionsPresenter.Factory,
) : DialogOverlay<SaveDefaultOptionsResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<SaveDefaultOptionsResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            presenterFactory.create(target = target, navigator = navigator)
        }
        SaveDefaultOptions(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(target: SaveDefaultOptionsTarget): SaveDefaultOptionsDialog
    }
}
