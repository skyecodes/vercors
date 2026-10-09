package app.vercors.feature.config.ui.datadirectory

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.core.domain.AppConstants
import app.vercors.feature.config.domain.usecase.MoveDataDirectoryUseCase
import app.vercors.shared.ui.AppRestartService
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Named
import kotlinx.coroutines.CoroutineScope

// On success this never finishes: AppRestartService.restart ends the process
@AssistedInject
class ChangeDataDirectoryDialog(
    @Assisted private val currentPath: String,
    @Named(AppConstants.APP_ID_KEY) private val appId: String,
    private val globalScope: CoroutineScope,
    private val moveDataDirectory: MoveDataDirectoryUseCase,
    private val appRestartService: AppRestartService,
) : DialogOverlay<Unit>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<Unit>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            ChangeDataDirectoryPresenter(
                currentPath = currentPath,
                appId = appId,
                navigator = navigator,
                globalScope = globalScope,
                moveDataDirectory = moveDataDirectory,
                appRestartService = appRestartService,
            )
        }
        ChangeDataDirectory(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(currentPath: String): ChangeDataDirectoryDialog
    }
}
