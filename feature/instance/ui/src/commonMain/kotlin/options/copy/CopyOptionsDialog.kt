package app.vercors.feature.instance.ui.options.copy

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

sealed interface CopyOptionsResult {
    data object Copied : CopyOptionsResult
    data object Cancelled : CopyOptionsResult
}

@AssistedInject
class CopyOptionsDialog(
    @Assisted private val target: CopyOptionsTarget,
    private val presenterFactory: CopyOptionsPresenter.Factory,
) : DialogOverlay<CopyOptionsResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<CopyOptionsResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            presenterFactory.create(target = target, navigator = navigator)
        }
        CopyOptions(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(target: CopyOptionsTarget): CopyOptionsDialog
    }
}
