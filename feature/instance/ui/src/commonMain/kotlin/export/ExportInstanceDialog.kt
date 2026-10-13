package app.vercors.feature.instance.ui.export

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.usecase.ListExportableEntriesUseCase
import app.vercors.feature.project.domain.usecase.ExportInstanceUseCase
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope

sealed interface ExportInstanceResult {
    data object Exported : ExportInstanceResult
    data object Cancelled : ExportInstanceResult
}

@AssistedInject
class ExportInstanceDialog(
    @Assisted private val target: ExportInstanceTarget,
    private val globalScope: CoroutineScope,
    private val listExportableEntries: ListExportableEntriesUseCase,
    private val exportInstance: ExportInstanceUseCase,
    private val snackbarService: SnackbarService,
) : DialogOverlay<ExportInstanceResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<ExportInstanceResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            ExportInstancePresenter(
                target = target,
                navigator = navigator,
                globalScope = globalScope,
                listExportableEntries = listExportableEntries,
                exportInstance = exportInstance,
                snackbarService = snackbarService
            )
        }
        ExportInstance(store = presenter.present())
    }

    @AssistedFactory
    fun interface Factory {
        fun create(target: ExportInstanceTarget): ExportInstanceDialog
    }
}
