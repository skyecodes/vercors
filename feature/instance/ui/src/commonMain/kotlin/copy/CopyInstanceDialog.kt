package app.vercors.feature.instance.ui.copy

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.CopyInstanceUseCase
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import kotlinx.coroutines.CoroutineScope

sealed interface CopyInstanceResult {
    data class Copied(val instanceId: InstanceId) : CopyInstanceResult
    data object Cancelled : CopyInstanceResult
}

data class CopyInstanceTarget(val id: InstanceId, val name: String)

/** Shown as a dialog overlay rather than a full navigated screen — see [CreateInstanceDialog][app.vercors.feature.instance.ui.create.CreateInstanceDialog] for the same pattern. */
class CopyInstanceDialog(
    private val target: CopyInstanceTarget,
    private val globalScope: CoroutineScope,
    private val copyInstance: CopyInstanceUseCase,
    private val snackbarService: SnackbarService,
) : DialogOverlay<CopyInstanceResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<CopyInstanceResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            CopyInstancePresenter(
                target = target,
                navigator = navigator,
                globalScope = globalScope,
                copyInstance = copyInstance,
                snackbarService = snackbarService
            )
        }
        CopyInstance(store = presenter.present())
    }
}
