package app.vercors.feature.instance.ui.create

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.game.domain.usecase.ObserveAvailableLoadersUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.domain.usecase.ObserveLoaderVersionsUseCase
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.instance.ui.install.LocalPackUpdateLauncher
import app.vercors.feature.project.domain.usecase.FindLocalPackTargetsUseCase
import app.vercors.feature.project.domain.usecase.InstallLocalPackUseCase
import app.vercors.feature.project.domain.usecase.ParseLocalPackUseCase
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope

sealed interface CreateInstanceResult {
    data class Created(val instanceId: InstanceId) : CreateInstanceResult
    data class Updated(val instanceId: InstanceId) : CreateInstanceResult
    data object Cancelled : CreateInstanceResult
    data object SearchModpacks : CreateInstanceResult
}

@Inject
class CreateInstanceDialog(
    private val globalScope: CoroutineScope,
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val observeAvailableLoaders: ObserveAvailableLoadersUseCase,
    private val observeLoaderVersions: ObserveLoaderVersionsUseCase,
    private val createInstance: CreateInstanceUseCase,
    private val snackbarService: SnackbarService,
    private val parseLocalPack: ParseLocalPackUseCase,
    private val findLocalPackTargets: FindLocalPackTargetsUseCase,
    private val installLocalPack: InstallLocalPackUseCase,
    private val localPackUpdateLauncher: LocalPackUpdateLauncher,
    private val setInstanceIcon: SetInstanceIconUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
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
                observeAvailableLoaders = observeAvailableLoaders,
                observeLoaderVersions = observeLoaderVersions,
                createInstance = createInstance,
                snackbarService = snackbarService,
                parseLocalPack = parseLocalPack,
                findLocalPackTargets = findLocalPackTargets,
                installLocalPack = installLocalPack,
                localPackUpdateLauncher = localPackUpdateLauncher,
                setInstanceIcon = setInstanceIcon,
                updateInstanceStatus = updateInstanceStatus,
                updateInstanceJob = updateInstanceJob,
                deleteInstance = deleteInstance,
            )
        }
        CreateInstance(store = presenter.present())
    }
}
