package app.vercors.feature.instance.ui.create

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.game.domain.usecase.GetAvailableLoadersUseCase
import app.vercors.feature.game.domain.usecase.GetLoaderVersionsUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope

sealed interface CreateInstanceResult {
    data class Created(val instanceId: InstanceId) : CreateInstanceResult
    data object Cancelled : CreateInstanceResult
}

/** Shown as a dialog overlay rather than a full navigated screen — see [ConfirmDeleteDialog][app.vercors.feature.instance.ui.delete.ConfirmDeleteDialog] for the same pattern. */
@Inject
class CreateInstanceDialog(
    private val globalScope: CoroutineScope,
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val getAvailableLoaders: GetAvailableLoadersUseCase,
    private val getLoaderVersions: GetLoaderVersionsUseCase,
    private val createInstance: CreateInstanceUseCase,
    private val snackbarService: SnackbarService
) : DialogOverlay<CreateInstanceResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<CreateInstanceResult>,
        transitionController: OverlayTransitionController,
    ) {
        val presenter = remember(navigator) {
            CreateInstancePresenter(
                navigator = navigator,
                globalScope = globalScope,
                observeGameVersionList = observeGameVersionList,
                getAvailableLoaders = getAvailableLoaders,
                getLoaderVersions = getLoaderVersions,
                createInstance = createInstance,
                snackbarService = snackbarService
            )
        }
        CreateInstance(store = presenter.present())
    }
}
