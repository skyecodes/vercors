package app.vercors.feature.instance.ui.options.copy

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.usecase.CopyOptionsUseCase
import app.vercors.feature.instance.domain.usecase.ObserveDefaultOptionsUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope

sealed interface CopyOptionsResult {
    data object Copied : CopyOptionsResult
    data object Cancelled : CopyOptionsResult
}

@AssistedInject
class CopyOptionsDialog(
    @Assisted private val target: CopyOptionsTarget,
    private val globalScope: CoroutineScope,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val observeDefaultOptions: ObserveDefaultOptionsUseCase,
    private val copyOptions: CopyOptionsUseCase,
    private val snackbarService: SnackbarService,
) : DialogOverlay<CopyOptionsResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<CopyOptionsResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            CopyOptionsPresenter(
                target = target,
                navigator = navigator,
                globalScope = globalScope,
                observeInstanceList = observeInstanceList,
                observeDefaultOptions = observeDefaultOptions,
                copyOptions = copyOptions,
                snackbarService = snackbarService,
            )
        }
        CopyOptions(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(target: CopyOptionsTarget): CopyOptionsDialog
    }
}
