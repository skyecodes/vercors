package app.vercors.feature.instance.ui.create

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.vercors.feature.game.domain.usecase.ObserveAvailableLoadersUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.domain.usecase.ObserveLoaderVersionsUseCase
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.project.domain.usecase.*
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope

sealed interface CreateInstanceResult {
    data class Created(val instanceId: InstanceId) : CreateInstanceResult
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
    private val parsePrismInstance: ParsePrismInstanceUseCase,
    private val importPrismInstanceFiles: ImportPrismInstanceFilesUseCase,
    private val parseLocalModpack: ParseLocalModpackUseCase,
    private val installModpackFiles: InstallModpackFilesUseCase,
    private val parseVercorsPack: ParseVercorsPackUseCase,
    private val extractVercorsPackIcon: ExtractVercorsPackIconUseCase,
    private val recordModpackFileOrigins: RecordModpackFileOriginsUseCase,
    private val setInstanceIcon: SetInstanceIconUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val refreshInstalledContent: RefreshInstalledContentUseCase,
    private val refreshInstalledWorlds: RefreshInstalledWorldsUseCase,
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
                parsePrismInstance = parsePrismInstance,
                importPrismInstanceFiles = importPrismInstanceFiles,
                parseLocalModpack = parseLocalModpack,
                installModpackFiles = installModpackFiles,
                parseVercorsPack = parseVercorsPack,
                extractVercorsPackIcon = extractVercorsPackIcon,
                recordModpackFileOrigins = recordModpackFileOrigins,
                setInstanceIcon = setInstanceIcon,
                updateInstanceStatus = updateInstanceStatus,
                updateInstanceJob = updateInstanceJob,
                deleteInstance = deleteInstance,
                refreshInstalledContent = refreshInstalledContent,
                refreshInstalledWorlds = refreshInstalledWorlds,
            )
        }
        CreateInstance(store = presenter.present())
    }
}
