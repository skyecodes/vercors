package app.vercors.feature.instance.ui.export

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

sealed interface ExportInstanceResult {
    data object Exported : ExportInstanceResult
    data object Cancelled : ExportInstanceResult
}

@AssistedInject
class ExportInstanceDialog(
    @Assisted private val target: ExportInstanceTarget,
    private val presenterFactory: ExportInstancePresenter.Factory,
) : DialogOverlay<ExportInstanceResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<ExportInstanceResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            presenterFactory.create(target = target, navigator = navigator)
        }
        ExportInstance(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(target: ExportInstanceTarget): ExportInstanceDialog
    }
}
