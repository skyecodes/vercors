package app.vercors.feature.instance.ui.copy

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

sealed interface CopyInstanceResult {
    data class Copied(val instanceId: InstanceId) : CopyInstanceResult
    data object Cancelled : CopyInstanceResult
}

@AssistedInject
class CopyInstanceDialog(
    @Assisted private val target: CopyInstanceTarget,
    private val presenterFactory: CopyInstancePresenter.Factory,
) : DialogOverlay<CopyInstanceResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<CopyInstanceResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            presenterFactory.create(target = target, navigator = navigator)
        }
        CopyInstance(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(target: CopyInstanceTarget): CopyInstanceDialog
    }
}
