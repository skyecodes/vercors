package app.vercors.feature.instance.ui.options.savedefault

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.usecase.ObserveDefaultOptionsUseCase
import app.vercors.feature.instance.domain.usecase.SaveDefaultOptionsUseCase
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope

sealed interface SaveDefaultOptionsResult {
    data object Saved : SaveDefaultOptionsResult
    data object Cancelled : SaveDefaultOptionsResult
}

@AssistedInject
class SaveDefaultOptionsDialog(
    @Assisted private val target: SaveDefaultOptionsTarget,
    private val globalScope: CoroutineScope,
    private val observeDefaultOptions: ObserveDefaultOptionsUseCase,
    private val saveDefaultOptions: SaveDefaultOptionsUseCase,
    private val snackbarService: SnackbarService,
) : DialogOverlay<SaveDefaultOptionsResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<SaveDefaultOptionsResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            SaveDefaultOptionsPresenter(
                target = target,
                navigator = navigator,
                globalScope = globalScope,
                observeDefaultOptions = observeDefaultOptions,
                saveDefaultOptions = saveDefaultOptions,
                snackbarService = snackbarService,
            )
        }
        SaveDefaultOptions(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(target: SaveDefaultOptionsTarget): SaveDefaultOptionsDialog
    }
}
