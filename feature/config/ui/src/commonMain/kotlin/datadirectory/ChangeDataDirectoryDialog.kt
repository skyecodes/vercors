package app.vercors.feature.config.ui.datadirectory

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.config.domain.usecase.MoveDataDirectoryUseCase
import app.vercors.shared.ui.AppRestartService
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import kotlinx.coroutines.CoroutineScope

/** Shown as a dialog overlay rather than a full navigated screen — see [CopyInstanceDialog][app.vercors.feature.instance.ui.copy.CopyInstanceDialog]
 *  for the same pattern. Finishes with `Unit` on cancel; on success it never returns — [AppRestartService.restart]
 *  ends the process before the dialog would otherwise finish. */
class ChangeDataDirectoryDialog(
    private val currentPath: String,
    private val appId: String,
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
}
