package app.vercors.feature.instance.ui.create

import androidx.compose.runtime.*
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.usecase.ObserveAvailableLoadersUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.domain.usecase.ObserveLoaderVersionsUseCase
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.instance.ui.install.LocalPackUpdateLauncher
import app.vercors.feature.instance.ui.message
import app.vercors.feature.instance.ui.observeAvailableLoadersView
import app.vercors.feature.instance.ui.observeGameVersionListView
import app.vercors.feature.instance.ui.observeLoaderVersionsView
import app.vercors.feature.project.domain.model.pack.LocalPack
import app.vercors.feature.project.domain.usecase.FindLocalPackTargetsUseCase
import app.vercors.feature.project.domain.usecase.InstallLocalPackUseCase
import app.vercors.feature.project.domain.usecase.ParseLocalPackUseCase
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import arrow.core.raise.recover
import com.slack.circuit.overlay.OverlayNavigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull

private val logger = KotlinLogging.logger {}

@AssistedInject
class CreateInstancePresenter(
    @Assisted private val navigator: OverlayNavigator<CreateInstanceResult>,
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
) : Presenter<CreateInstanceState, CreateInstanceEvent> {
    @Composable
    override fun present(): Store<CreateInstanceState, CreateInstanceEvent> {
        val gameVersionList by observeGameVersionListView(observeGameVersionList)
        val (selectedInstanceType, setSelectedInstanceType) = remember { mutableStateOf(InstanceType.Custom) }
        val (instanceName, setInstanceName) = remember { mutableStateOf("") }
        val (selectedGameVersion, setSelectedGameVersion) = remember { mutableStateOf<GameVersionView?>(null) }
        val (selectedLoader, setSelectedLoader) = remember { mutableStateOf<ModLoaderType?>(null) }
        val (selectedLoaderVersion, setSelectedLoaderVersion) = remember { mutableStateOf<LoaderVersion?>(null) }
        val loaderList by observeAvailableLoadersView(observeAvailableLoaders, selectedGameVersion?.id)
        val loaderVersionList by observeLoaderVersionsView(
            observeLoaderVersions,
            selectedGameVersion?.id,
            selectedLoader
        )
        val (importParsing, setImportParsing) = remember { mutableStateOf(false) }
        val importState = rememberImportState()

        LaunchedEffect(loaderVersionList) {
            if (selectedLoaderVersion == null) {
                setSelectedLoaderVersion(
                    loaderVersionList.valueOrNull?.let { versions ->
                        versions.firstOrNull { it.stable } ?: versions.firstOrNull()
                    }
                )
            }
        }

        return CreateInstanceState(
            instanceName = instanceName,
            selectedGameVersion = selectedGameVersion,
            gameVersionList = gameVersionList,
            selectedLoader = selectedLoader,
            loaderList = loaderList,
            selectedLoaderVersion = selectedLoaderVersion,
            loaderVersionList = loaderVersionList,
            selectedInstanceType = selectedInstanceType,
            importParsing = importParsing,
            importPreview = importState.preview,
            importTargets = importState.targets,
            selectedImportTargetId = importState.selectedTargetId,
            importInstanceName = importState.instanceName,
            importing = importState.importing,
        ) + { event ->
            handleEvent(
                event = event,
                setSelectedInstanceType = setSelectedInstanceType,
                instanceName = instanceName,
                setInstanceName = setInstanceName,
                selectedGameVersion = selectedGameVersion,
                setSelectedGameVersion = setSelectedGameVersion,
                selectedLoader = selectedLoader,
                setSelectedLoader = setSelectedLoader,
                selectedLoaderVersion = selectedLoaderVersion,
                setSelectedLoaderVersion = setSelectedLoaderVersion,
                setImportParsing = setImportParsing,
                importState = importState,
            )
        }
    }

    @Composable
    private fun rememberImportState(): ImportState {
        val (preview, setPreview) = remember { mutableStateOf<LocalPack?>(null) }
        val (targets, setTargets) = remember { mutableStateOf(emptyList<ImportTargetView>()) }
        val (selectedTargetId, setSelectedTargetId) = remember { mutableStateOf<InstanceId?>(null) }
        val (instanceName, setInstanceName) = remember { mutableStateOf("") }
        val (importing, setImporting) = remember { mutableStateOf(false) }
        return ImportState(
            preview,
            setPreview,
            targets,
            setTargets,
            selectedTargetId,
            setSelectedTargetId,
            instanceName,
            setInstanceName,
            importing,
            setImporting
        )
    }

    private fun handleEvent(
        event: CreateInstanceEvent,
        setSelectedInstanceType: (InstanceType) -> Unit,
        instanceName: String,
        setInstanceName: (String) -> Unit,
        selectedGameVersion: GameVersionView?,
        setSelectedGameVersion: (GameVersionView?) -> Unit,
        selectedLoader: ModLoaderType?,
        setSelectedLoader: (ModLoaderType?) -> Unit,
        selectedLoaderVersion: LoaderVersion?,
        setSelectedLoaderVersion: (LoaderVersion?) -> Unit,
        setImportParsing: (Boolean) -> Unit,
        importState: ImportState,
    ) {
        when (event) {
            is CreateInstanceEvent.OnInstanceTypeChange -> setSelectedInstanceType(event.type)
            is CreateInstanceEvent.OnInstanceNameChange -> setInstanceName(event.name)
            is CreateInstanceEvent.OnGameVersionChange -> {
                setSelectedGameVersion(event.version)
                setSelectedLoader(null)
                setSelectedLoaderVersion(null)
            }

            is CreateInstanceEvent.OnLoaderChange -> {
                setSelectedLoader(event.loader)
                setSelectedLoaderVersion(null)
            }

            is CreateInstanceEvent.OnLoaderVersionChange -> setSelectedLoaderVersion(event.version)
            CreateInstanceEvent.OnCancelClick -> navigator.finish(CreateInstanceResult.Cancelled)
            CreateInstanceEvent.OnCreateInstanceClick ->
                createInstance(instanceName, selectedGameVersion, selectedLoader, selectedLoaderVersion)

            CreateInstanceEvent.OnOpenModpacksScreen -> navigator.finish(CreateInstanceResult.SearchModpacks)
            is CreateInstanceEvent.OnImportFileSelected ->
                handleImportFileSelected(event.sourcePath, importState, setImportParsing)

            is CreateInstanceEvent.OnImportInstanceNameChange -> importState.setInstanceName(event.name)
            is CreateInstanceEvent.OnImportTargetSelected -> importState.setSelectedTargetId(event.instanceId)
            CreateInstanceEvent.OnConfirmImportClick -> handleConfirmImport(importState)
        }
    }

    private fun createInstance(
        instanceName: String,
        selectedGameVersion: GameVersionView?,
        selectedLoader: ModLoaderType?,
        selectedLoaderVersion: LoaderVersion?
    ) {
        val instance = Instance(
            name = instanceName,
            gameVersion = selectedGameVersion!!.id,
            modLoader = selectedLoader?.let { InstanceModLoader(type = it, version = selectedLoaderVersion!!.version) }
        )
        var createdInstance = instance
        globalScope.launch {
            recover({ createdInstance = createInstance(instance) }) { error -> snackbarService.error { error.message } }
        }.invokeOnCompletion { error ->
            if (error == null) navigator.finish(CreateInstanceResult.Created(createdInstance.id))
        }
    }

    private fun handleImportFileSelected(
        sourcePath: String,
        importState: ImportState,
        setImportParsing: (Boolean) -> Unit,
    ) {
        importState.setPreview(null)
        importState.setTargets(emptyList())
        importState.setSelectedTargetId(null)
        setImportParsing(true)
        globalScope.launch {
            val pack = either { parseLocalPack(sourcePath) }
                .onLeft { error -> snackbarService.error { error.message } }
                .getOrNull()
            importState.setPreview(pack)
            importState.setInstanceName(pack?.name.orEmpty())
            importState.setTargets(
                pack?.let { findLocalPackTargets(it) }.orEmpty().map { ImportTargetView(it.id, it.name) }
            )
            setImportParsing(false)
        }
    }

    private fun handleConfirmImport(importState: ImportState) {
        val pack = importState.preview ?: return
        val target = importState.targets.find { it.id == importState.selectedTargetId }
        if (target != null) {
            localPackUpdateLauncher.launch(target.id, target.name, pack)
            navigator.finish(CreateInstanceResult.Updated(target.id))
        } else {
            importAsNewInstance(pack, importState.instanceName, importState.setImporting)
        }
    }

    // Rolled back only on cancellation: an install failure leaves the instance visible as InstallFailed
    private fun importAsNewInstance(pack: LocalPack, instanceName: String, setImporting: (Boolean) -> Unit) {
        if (instanceName.length < 3) return
        setImporting(true)
        val vercorsInfo = (pack as? LocalPack.Vercors)?.info
        val instance = Instance(
            name = instanceName,
            gameVersion = pack.gameVersion,
            modLoader = pack.loaderType?.let { InstanceModLoader(type = it, version = pack.loaderVersion!!) },
            modpackInfo = pack.modpackInfo,
            jvmMemoryOptions = vercorsInfo?.jvmMemoryOptions ?: JvmMemoryOptions(),
            nativeLibraryOptions = vercorsInfo?.nativeLibraryOptions ?: NativeLibraryOptions(),
        )
        var createdInstanceId: InstanceId? = null
        lateinit var job: Job
        job = globalScope.launch {
            val created = recover({ createInstance(instance) }) { error: InstanceValidationError ->
                snackbarService.error { error.message }
                null
            }
            if (created == null) {
                setImporting(false)
                return@launch
            }
            createdInstanceId = created.id
            updateInstanceJob(created.id, job)
            setImporting(false)
            navigator.finish(CreateInstanceResult.Created(created.id))

            val result = either { installLocalPack(pack, created.id, isUpdate = false) }
                .onLeft { error -> logger.error { "Local pack import failed for ${created.id}: $error" } }
                .getOrNull()
            result?.iconPath?.let { setImportedIcon(created.id, it) }
            updateInstanceStatus(
                created.id,
                if (result != null) InstanceStatus.Idle else InstanceStatus.Failed.InstallFailed
            )
        }
        job.invokeOnCompletion { error ->
            createdInstanceId?.let { updateInstanceJob(it, null) }
            if (error != null) createdInstanceId?.let { id ->
                globalScope.launch { recover({ deleteInstance(id) }) { _: InstanceError -> } }
            }
        }
    }

    private suspend fun setImportedIcon(instanceId: InstanceId, iconPath: String) {
        recover({ setInstanceIcon(instanceId, iconPath) }) { error: InstanceError ->
            logger.warn { "Failed to set imported instance icon for $instanceId: $error" }
        }
    }

    private data class ImportState(
        val preview: LocalPack?,
        val setPreview: (LocalPack?) -> Unit,
        val targets: List<ImportTargetView>,
        val setTargets: (List<ImportTargetView>) -> Unit,
        val selectedTargetId: InstanceId?,
        val setSelectedTargetId: (InstanceId?) -> Unit,
        val instanceName: String,
        val setInstanceName: (String) -> Unit,
        val importing: Boolean,
        val setImporting: (Boolean) -> Unit,
    )

    @AssistedFactory
    fun interface Factory {
        fun create(navigator: OverlayNavigator<CreateInstanceResult>): CreateInstancePresenter
    }
}
