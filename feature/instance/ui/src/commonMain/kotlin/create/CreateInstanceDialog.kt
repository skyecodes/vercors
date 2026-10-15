package app.vercors.feature.instance.ui.create

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Inject

sealed interface CreateInstanceResult {
    data class Created(val instanceId: InstanceId) : CreateInstanceResult
    data class Updated(val instanceId: InstanceId) : CreateInstanceResult
    data object Cancelled : CreateInstanceResult
    data object SearchModpacks : CreateInstanceResult
}

@Inject
class CreateInstanceDialog(
    private val presenterFactory: CreateInstancePresenter.Factory,
) : DialogOverlay<CreateInstanceResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<CreateInstanceResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            presenterFactory.create(navigator = navigator)
        }
        CreateInstance(store = presenter.present())
    }
}
