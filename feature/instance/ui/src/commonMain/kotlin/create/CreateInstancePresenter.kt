package app.vercors.feature.instance.ui.create

import androidx.compose.runtime.*
import app.vercors.core.domain.toFractionOrNull
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.usecase.ObserveAvailableLoadersUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.domain.usecase.ObserveLoaderVersionsUseCase
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.domain.model.localimport.LocalImportError
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.instance.ui.*
import app.vercors.feature.project.domain.model.modpack.ModpackInstallError
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import app.vercors.feature.project.domain.usecase.InstallModpackFilesUseCase
import app.vercors.feature.project.domain.usecase.ParseLocalModpackUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.recover
import com.slack.circuit.overlay.OverlayNavigator
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

private val logger = KotlinLogging.logger {}

class CreateInstancePresenter(
    private val navigator: OverlayNavigator<CreateInstanceResult>,
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
    private val setInstanceIcon: SetInstanceIconUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val refreshInstalledContent: RefreshInstalledContentUseCase,
    private val refreshInstalledWorlds: RefreshInstalledWorldsUseCase,
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
        val (importPreview, setImportPreview) = remember { mutableStateOf<LocalImportPreview?>(null) }
        val (importInstanceName, setImportInstanceName) = remember { mutableStateOf("") }
        val (importing, setImporting) = remember { mutableStateOf(false) }

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
            importPreview = importPreview,
            importInstanceName = importInstanceName,
            importing = importing,
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
                setImportPreview = setImportPreview,
                importInstanceName = importInstanceName,
                setImportInstanceName = setImportInstanceName,
                importPreview = importPreview,
                setImporting = setImporting,
            )
        }
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
        setImportPreview: (LocalImportPreview?) -> Unit,
        importInstanceName: String,
        setImportInstanceName: (String) -> Unit,
        importPreview: LocalImportPreview?,
        setImporting: (Boolean) -> Unit,
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
                handleImportFileSelected(event.sourcePath, setImportPreview, setImportInstanceName, setImportParsing)

            is CreateInstanceEvent.OnImportInstanceNameChange -> setImportInstanceName(event.name)
            CreateInstanceEvent.OnConfirmImportClick -> handleConfirmImport(
                importPreview,
                importInstanceName,
                setImporting
            )
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

    // Prism format first, then .mrpack, matching PrismLauncher's own detection order
    private fun handleImportFileSelected(
        sourcePath: String,
        setImportPreview: (LocalImportPreview?) -> Unit,
        setImportInstanceName: (String) -> Unit,
        setImportParsing: (Boolean) -> Unit,
    ) {
        setImportPreview(null)
        setImportParsing(true)
        globalScope.launch {
            val preview = recover({
                val info = parsePrismInstance(sourcePath)
                LocalImportPreview.Prism(
                    info.name,
                    info.gameVersion,
                    info.loaderType,
                    info.loaderVersion,
                    sourcePath,
                    info
                )
            }) { error: LocalImportError ->
                if (error == LocalImportError.NotPrismFormat) tryParseAsMrpack(sourcePath) else null
            }
            setImportPreview(preview)
            setImportInstanceName(preview?.name.orEmpty())
            setImportParsing(false)
        }
    }

    private suspend fun tryParseAsMrpack(sourcePath: String): LocalImportPreview.Mrpack? =
        recover({
            val manifest = parseLocalModpack(sourcePath, platformId = "modrinth")
            LocalImportPreview.Mrpack(
                manifest.name,
                manifest.gameVersion,
                manifest.loaderType,
                manifest.loaderVersion,
                sourcePath,
                manifest
            )
        }) { _: ModpackResolutionError ->
            snackbarService.error { stringResource(InstanceRes.string.errorImportUnsupportedFormat) }
            null
        }

    // Rolled back only on cancellation: an install failure leaves the instance visible as InstallFailed
    private fun handleConfirmImport(
        preview: LocalImportPreview?,
        instanceName: String,
        setImporting: (Boolean) -> Unit
    ) {
        if (preview == null || instanceName.length < 3) return
        setImporting(true)
        val instance = Instance(
            name = instanceName,
            gameVersion = preview.gameVersion,
            modLoader = preview.loaderType?.let { InstanceModLoader(type = it, version = preview.loaderVersion!!) },
            modpackInfo = preview.modpackInfo,
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

            val ok = when (preview) {
                is LocalImportPreview.Prism -> runPrismImport(preview, created.id)
                is LocalImportPreview.Mrpack -> runMrpackImport(preview, created.id)
            }
            updateInstanceStatus(created.id, if (ok) InstanceStatus.Idle else InstanceStatus.Failed.InstallFailed)
        }
        job.invokeOnCompletion { error ->
            createdInstanceId?.let { updateInstanceJob(it, null) }
            if (error != null) createdInstanceId?.let { id ->
                globalScope.launch { recover({ deleteInstance(id) }) { _: InstanceError -> } }
            }
        }
    }

    private suspend fun runPrismImport(preview: LocalImportPreview.Prism, instanceId: InstanceId): Boolean {
        var iconPath: String? = null
        val ok = recover({
            iconPath = importPrismInstanceFiles(preview.sourcePath, instanceId, preview.info) { progress ->
                updateInstanceStatus(instanceId, InstanceStatus.Installing.ExtractingFiles(progress.toFractionOrNull()))
            }
            true
        }) { error: LocalImportError ->
            logger.error { "Prism import failed for $instanceId: $error" }
            false
        }
        if (ok) {
            // Bulk write outside the scanners' APIs: refresh explicitly rather than relying on the watcher
            refreshInstalledContent(ContentKind.Mod, instanceId)
            refreshInstalledContent(ContentKind.ResourcePack, instanceId)
            refreshInstalledContent(ContentKind.ShaderPack, instanceId)
            refreshInstalledWorlds(instanceId)
            iconPath?.let { path ->
                recover({ setInstanceIcon(instanceId, path) }) { error: InstanceError ->
                    logger.warn { "Failed to set imported instance icon for $instanceId: $error" }
                }
            }
        }
        return ok
    }

    // Local mrpack files always have a download URL, so there's never a manual download to await
    private suspend fun runMrpackImport(preview: LocalImportPreview.Mrpack, instanceId: InstanceId): Boolean =
        recover({
            installModpackFiles(preview.manifest, instanceId, localSourcePath = preview.sourcePath) { progress ->
                updateInstanceStatus(
                    instanceId,
                    InstanceStatus.Installing.DownloadingFiles(progress.toFractionOrNull())
                )
            }
            true
        }) { error: ModpackInstallError ->
            logger.error { "Local modpack import failed for $instanceId: $error" }
            false
        }
}
