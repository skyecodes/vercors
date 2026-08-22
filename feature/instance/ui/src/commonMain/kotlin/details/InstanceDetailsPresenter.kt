package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import app.vercors.core.ui.state.mutableProgressiveStateOf
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.usecase.*
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.game.ui.toView
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.install.FinishInstallationDialog
import app.vercors.feature.instance.ui.list.InstanceListScreen
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.usecase.CancelInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.KillInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.StopInstanceUseCase
import app.vercors.feature.project.domain.model.from
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.domain.usecase.GetTotalPhysicalMemoryUseCase
import app.vercors.shared.ui.*
import arrow.core.raise.context.either
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@AssistedInject
class InstanceDetailsPresenter(
    @Assisted private val screen: InstanceDetailsScreen,
    @Assisted private val navigator: Navigator,
    private val globalScope: CoroutineScope,
    private val observeInstance: ObserveInstanceUseCase,
    private val observeInstanceLogs: ObserveInstanceLogsUseCase,
    private val observeInstalledMods: ObserveInstalledModsUseCase,
    private val setModEnabled: SetModEnabledUseCase,
    private val removeMod: RemoveModUseCase,
    private val observeInstalledResourcePacks: ObserveInstalledResourcePacksUseCase,
    private val setResourcePackEnabled: SetResourcePackEnabledUseCase,
    private val removeResourcePack: RemoveResourcePackUseCase,
    private val observeInstalledShaderPacks: ObserveInstalledShaderPacksUseCase,
    private val setShaderPackEnabled: SetShaderPackEnabledUseCase,
    private val removeShaderPack: RemoveShaderPackUseCase,
    private val launchInstance: LaunchInstanceUseCase,
    private val cancelInstance: CancelInstanceUseCase,
    private val cancelInstanceSetup: CancelInstanceSetupUseCase,
    private val stopInstance: StopInstanceUseCase,
    private val killInstance: KillInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val snackbarService: SnackbarService,
    private val updateInstance: UpdateInstanceUseCase,
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val getAvailableLoaders: GetAvailableLoadersUseCase,
    private val getLoaderVersions: GetLoaderVersionsUseCase,
    private val observeJavaInstallations: ObserveJavaInstallationsUseCase,
    private val installJavaRuntime: InstallJavaRuntimeUseCase,
    private val observeManualDownloadItems: ObserveManualDownloadItemsUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val setInstanceIcon: SetInstanceIconUseCase,
    private val removeInstanceIcon: RemoveInstanceIconUseCase,
    private val importMod: ImportModUseCase,
    private val importResourcePack: ImportResourcePackUseCase,
    private val importShaderPack: ImportShaderPackUseCase,
    private val openInstanceFolder: OpenInstanceFolderUseCase,
    private val getTotalPhysicalMemory: GetTotalPhysicalMemoryUseCase,
) : Presenter<InstanceDetailsState, InstanceDetailsEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<InstanceDetailsState, InstanceDetailsEvent> {
        val instance by observeInstanceView()
        val gameVersionList by observeGameVersionListView()
        val javaInstallations by observeJavaInstallationsView()
        val (promptInstallJava, setPromptInstallJava) = remember { mutableStateOf<Int?>(null) }
        val logs by observeInstanceLogsView()
        val installedMods by observeInstalledModsView()
        val installedResourcePacks by observeInstalledResourcePacksView()
        val installedShaderPacks by observeInstalledShaderPacksView()
        val (promptDeleteConfirmation, setPromptDeleteConfirmation) = rememberSaveable { mutableStateOf(false) }
        val (promptRemoveConfirmation, setPromptRemoveConfirmation) =
            remember { mutableStateOf<PendingRemoval?>(null) }
        val (promptFinishInstallation, setPromptFinishInstallation) = rememberSaveable { mutableStateOf(false) }
        val finishInstallationDialog = remember(screen.instanceId) {
            FinishInstallationDialog(screen.instanceId, observeManualDownloadItems) { url -> copyToClipboard(url) }
        }
        val (selectedTab, setSelectedTab) = rememberSaveable { mutableStateOf(InstanceDetailsTab.Options) }
        val (editedInstance, setEditedInstance) = rememberSaveable { mutableStateOf(InstanceEditState()) }
        val loaderList = remember { mutableProgressiveStateOf<LoaderError, List<ModLoaderType>>() }
        val loaderVersionList = remember { mutableProgressiveStateOf<LoaderError, List<LoaderVersion>>() }
        val totalMemoryMb = remember {
            ((getTotalPhysicalMemory() ?: (16384L * 1024 * 1024)) / (1024 * 1024)).toInt()
        }

        val currentInstance = instance.valueOrNull
        val effectiveGameVersion = editedInstance.gameVersion ?: currentInstance?.gameVersion
        val effectiveLoaderType = when (val edit = editedInstance.modLoaderType) {
            is ModLoaderTypeEdit.Loader -> edit.type
            ModLoaderTypeEdit.Vanilla -> null
            null -> currentInstance?.loaderType
        }

        LaunchedEffect(effectiveGameVersion) {
            loaderList.value = ProgressiveOutcome.Incomplete()
            if (effectiveGameVersion != null) {
                loaderList.value = progressive { getAvailableLoaders(effectiveGameVersion) }
            }
        }

        LaunchedEffect(effectiveGameVersion, effectiveLoaderType) {
            loaderVersionList.value = ProgressiveOutcome.Incomplete()
            if (effectiveGameVersion != null && effectiveLoaderType != null) {
                loaderVersionList.value = progressive { getLoaderVersions(effectiveGameVersion, effectiveLoaderType) }
            }
        }

        LaunchedEffect(editedInstance.modLoaderType, editedInstance.modLoaderVersion, loaderVersionList.value) {
            val versions = loaderVersionList.value.valueOrNull
            if (editedInstance.modLoaderType is ModLoaderTypeEdit.Loader &&
                editedInstance.modLoaderVersion == null &&
                versions != null
            ) {
                val recommended = versions.firstOrNull { it.stable } ?: versions.firstOrNull()
                if (recommended != null) {
                    setEditedInstance(editedInstance.copy(modLoaderVersion = recommended.version))
                }
            }
        }

        return InstanceDetailsState(
            instance = instance,
            editedInstance = editedInstance,
            gameVersionList = gameVersionList.valueOrNull ?: emptyList(),
            loaderList = loaderList.value.valueOrNull ?: emptyList(),
            loaderVersionList = loaderVersionList.value.valueOrNull ?: emptyList(),
            availableJavaInstallations = javaInstallations.valueOrNull ?: emptyList(),
            promptInstallJava = promptInstallJava,
            totalMemoryMb = totalMemoryMb,
            promptDeleteConfirmation = promptDeleteConfirmation,
            selectedTab = selectedTab,
            logs = logs,
            installedMods = installedMods,
            installedResourcePacks = installedResourcePacks,
            installedShaderPacks = installedShaderPacks,
            promptRemoveConfirmation = promptRemoveConfirmation,
            promptFinishInstallation = promptFinishInstallation,
            finishInstallationDialog = finishInstallationDialog
        ) + { event ->
            handleEvent(
                event = event,
                setPromptDeleteConfirmation = setPromptDeleteConfirmation,
                setSelectedTab = setSelectedTab,
                setEditedInstance = setEditedInstance,
                editedInstance = editedInstance,
                currentInstance = currentInstance,
                promptRemoveConfirmation = promptRemoveConfirmation,
                setPromptRemoveConfirmation = setPromptRemoveConfirmation,
                setPromptFinishInstallation = setPromptFinishInstallation,
                promptInstallJava = promptInstallJava,
                setPromptInstallJava = setPromptInstallJava
            )
        }
    }

    private fun handleEvent(
        event: InstanceDetailsEvent,
        setPromptDeleteConfirmation: (Boolean) -> Unit,
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        setEditedInstance: (InstanceEditState) -> Unit,
        editedInstance: InstanceEditState,
        currentInstance: InstanceView?,
        promptRemoveConfirmation: PendingRemoval?,
        setPromptRemoveConfirmation: (PendingRemoval?) -> Unit,
        setPromptFinishInstallation: (Boolean) -> Unit,
        promptInstallJava: Int?,
        setPromptInstallJava: (Int?) -> Unit
    ) {
        if (handleLifecycleEvent(
                event,
                setSelectedTab,
                setPromptDeleteConfirmation,
                promptInstallJava,
                setPromptInstallJava
            )
        ) return
        if (handleEditEvent(event, editedInstance, setEditedInstance, setSelectedTab)) return
        if (handleContentEvent(event, promptRemoveConfirmation, setPromptRemoveConfirmation)) return
        handleNavigationAndMiscEvent(event, currentInstance, setPromptFinishInstallation)
    }

    /** Launch/stop/delete and other whole-instance lifecycle events. Returns whether [event] was one of these. */
    private fun handleLifecycleEvent(
        event: InstanceDetailsEvent,
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        setPromptDeleteConfirmation: (Boolean) -> Unit,
        promptInstallJava: Int?,
        setPromptInstallJava: (Int?) -> Unit
    ): Boolean {
        when (event) {
            InstanceDetailsEvent.Launch -> handleLaunch(setSelectedTab, setPromptInstallJava)
            InstanceDetailsEvent.OnInstallJavaConfirm -> handleInstallJavaConfirm(
                promptInstallJava,
                setPromptInstallJava
            )

            InstanceDetailsEvent.OnInstallJavaDismiss -> setPromptInstallJava(null)
            InstanceDetailsEvent.Delete -> setPromptDeleteConfirmation(true)
            is InstanceDetailsEvent.ConfirmDelete -> handleConfirmDelete(event, setPromptDeleteConfirmation)
            InstanceDetailsEvent.Cancel -> cancelInstance(screen.instanceId)
            InstanceDetailsEvent.CancelSetup -> cancelInstanceSetup(screen.instanceId)
            InstanceDetailsEvent.Stop -> globalScope.launch { stopInstance(screen.instanceId) }
            InstanceDetailsEvent.Kill -> globalScope.launch { killInstance(screen.instanceId) }
            else -> return false
        }
        return true
    }

    /** Tab selection and the name/version/loader edit-and-save flow. Returns whether [event] was one of these. */
    private fun handleEditEvent(
        event: InstanceDetailsEvent,
        editedInstance: InstanceEditState,
        setEditedInstance: (InstanceEditState) -> Unit,
        setSelectedTab: (InstanceDetailsTab) -> Unit
    ): Boolean {
        when (event) {
            is InstanceDetailsEvent.SelectTab -> setSelectedTab(event.tab)
            is InstanceDetailsEvent.UpdateInstance -> setEditedInstance(event.update(editedInstance))
            is InstanceDetailsEvent.OnLoaderChange -> handleLoaderChange(event, editedInstance, setEditedInstance)
            is InstanceDetailsEvent.OnLoaderVersionChange ->
                setEditedInstance(editedInstance.copy(modLoaderVersion = event.version))

            is InstanceDetailsEvent.OnJavaOverrideChange -> setEditedInstance(
                editedInstance.copy(
                    javaOverride = event.installationId?.let(JavaOverrideEdit::Specific) ?: JavaOverrideEdit.UseDefault
                )
            )

            InstanceDetailsEvent.CancelEdit -> setEditedInstance(InstanceEditState())
            InstanceDetailsEvent.SaveInstance -> handleSaveInstance(editedInstance, setEditedInstance)
            else -> return false
        }
        return true
    }

    /** Enable/disable and removal of installed mods/resource packs/shader packs. Returns whether [event] was one of these. */
    private fun handleContentEvent(
        event: InstanceDetailsEvent,
        promptRemoveConfirmation: PendingRemoval?,
        setPromptRemoveConfirmation: (PendingRemoval?) -> Unit
    ): Boolean {
        when (event) {
            is InstanceDetailsEvent.OnModEnabledChange -> globalScope.launch {
                setModEnabled(screen.instanceId, event.fileName, event.enabled)
            }

            is InstanceDetailsEvent.OnResourcePackEnabledChange -> globalScope.launch {
                setResourcePackEnabled(screen.instanceId, event.fileName, event.enabled)
            }

            is InstanceDetailsEvent.OnShaderPackEnabledChange -> globalScope.launch {
                setShaderPackEnabled(screen.instanceId, event.fileName, event.enabled)
            }

            is InstanceDetailsEvent.OnRemoveClick ->
                setPromptRemoveConfirmation(PendingRemoval(event.kind, event.fileName, event.title))

            is InstanceDetailsEvent.OnConfirmRemove ->
                handleConfirmRemove(event, promptRemoveConfirmation, setPromptRemoveConfirmation)

            else -> return false
        }
        return true
    }

    /** Navigation, custom icon, and manual-download-installation events. */
    private fun handleNavigationAndMiscEvent(
        event: InstanceDetailsEvent,
        currentInstance: InstanceView?,
        setPromptFinishInstallation: (Boolean) -> Unit
    ) {
        when (event) {
            is InstanceDetailsEvent.OnOpenProjectClick -> navigator.goTo(
                ProjectDetailsScreen(event.projectId from event.platformId, preferredInstanceId = screen.instanceId)
            )

            is InstanceDetailsEvent.OnAddContentClick -> navigator.goTo(
                ProjectListScreen(
                    initialType = event.type,
                    initialGameVersion = currentInstance?.gameVersion,
                    initialLoader = currentInstance?.loaderType,
                    sourceInstanceId = screen.instanceId
                )
            )

            is InstanceDetailsEvent.OnIconSelected -> globalScope.launch {
                either {
                    setInstanceIcon(
                        screen.instanceId,
                        event.path
                    )
                }.onLeft { error -> snackbarService.error { error.message } }
            }

            InstanceDetailsEvent.OnRemoveIconClick -> globalScope.launch {
                either { removeInstanceIcon(screen.instanceId) }.onLeft { error -> snackbarService.error { error.message } }
            }

            is InstanceDetailsEvent.OnImportContentClick -> globalScope.launch {
                val imported = when (event.kind) {
                    RemovalKind.Mod -> importMod(screen.instanceId, event.path)
                    RemovalKind.ResourcePack -> importResourcePack(screen.instanceId, event.path)
                    RemovalKind.ShaderPack -> importShaderPack(screen.instanceId, event.path)
                }
                if (!imported) snackbarService.error { stringResource(InstanceRes.string.errorImportFailed) }
            }

            is InstanceDetailsEvent.OnOpenInstanceFolderClick -> globalScope.launch {
                val opened = openInstanceFolder(screen.instanceId, event.folder)
                if (!opened) snackbarService.error { stringResource(InstanceRes.string.errorOpenFolderFailed) }
            }

            InstanceDetailsEvent.OnFinishInstallationClick -> setPromptFinishInstallation(true)
            InstanceDetailsEvent.OnFinishInstallationDismiss -> setPromptFinishInstallation(false)
            else -> Unit
        }
    }

    private fun handleLaunch(
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        setPromptInstallJava: (Int?) -> Unit
    ) = globalScope.launch {
        setSelectedTab(InstanceDetailsTab.Logs)
        either { launchInstance(screen.instanceId) }.onLeft { error ->
            if (error is InstanceLaunchError.NoCompatibleJava) setPromptInstallJava(error.majorVersion)
            else snackbarService.error { error.message }
        }
    }

    private fun handleInstallJavaConfirm(promptInstallJava: Int?, setPromptInstallJava: (Int?) -> Unit) {
        setPromptInstallJava(null)
        val majorVersion = promptInstallJava ?: return
        globalScope.launch {
            either { installJavaRuntime(majorVersion) }
                .onLeft { error ->
                    snackbarService.error {
                        stringResource(
                            InstanceRes.string.javaInstallFailed,
                            majorVersion,
                            error.toString()
                        )
                    }
                }
                .onRight {
                    snackbarService.success {
                        stringResource(
                            InstanceRes.string.javaInstallSucceeded,
                            majorVersion
                        )
                    }
                }
        }
    }

    private fun handleConfirmDelete(
        event: InstanceDetailsEvent.ConfirmDelete,
        setPromptDeleteConfirmation: (Boolean) -> Unit
    ) {
        setPromptDeleteConfirmation(false)
        if (!event.result) return
        globalScope.launch {
            either { deleteInstance(screen.instanceId) }
                .onLeft { error -> snackbarService.error { error.message } }
                .onRight {
                    navigator.pop()
                    navigator.goTo(InstanceListScreen)
                }
        }
    }

    private fun handleLoaderChange(
        event: InstanceDetailsEvent.OnLoaderChange,
        editedInstance: InstanceEditState,
        setEditedInstance: (InstanceEditState) -> Unit
    ) = setEditedInstance(
        editedInstance.copy(
            modLoaderType = event.loader?.let(ModLoaderTypeEdit::Loader) ?: ModLoaderTypeEdit.Vanilla,
            // Reset so the recommended-version effect can pick one that's valid for the new type.
            modLoaderVersion = null
        )
    )

    private fun handleConfirmRemove(
        event: InstanceDetailsEvent.OnConfirmRemove,
        promptRemoveConfirmation: PendingRemoval?,
        setPromptRemoveConfirmation: (PendingRemoval?) -> Unit
    ) {
        setPromptRemoveConfirmation(null)
        if (!event.result || promptRemoveConfirmation == null) return
        globalScope.launch {
            when (promptRemoveConfirmation.kind) {
                RemovalKind.Mod -> removeMod(screen.instanceId, promptRemoveConfirmation.fileName)
                RemovalKind.ResourcePack -> removeResourcePack(screen.instanceId, promptRemoveConfirmation.fileName)
                RemovalKind.ShaderPack -> removeShaderPack(screen.instanceId, promptRemoveConfirmation.fileName)
            }
        }
    }

    private fun handleSaveInstance(editedInstance: InstanceEditState, setEditedInstance: (InstanceEditState) -> Unit) =
        globalScope.launch {
            either {
                updateInstance(screen.instanceId) { it.withEdits(editedInstance).copy(updatedAt = Clock.System.now()) }
                setEditedInstance(InstanceEditState())
            }.onLeft { error -> snackbarService.error { error.toString() } }
        }

    private fun Instance.withEdits(editedInstance: InstanceEditState): Instance {
        var updated = editedInstance.name?.let { name -> copy(name = name) } ?: this
        updated = editedInstance.gameVersion?.let { version -> updated.copy(gameVersion = version) } ?: updated

        if (editedInstance.modLoaderType != null || editedInstance.modLoaderVersion != null) {
            val type = when (val edit = editedInstance.modLoaderType) {
                is ModLoaderTypeEdit.Loader -> edit.type
                ModLoaderTypeEdit.Vanilla -> null
                null -> updated.modLoader?.type
            }
            updated = updated.copy(
                modLoader = type?.let { loaderType ->
                    InstanceModLoader(
                        type = loaderType,
                        version = editedInstance.modLoaderVersion ?: updated.modLoader?.version.orEmpty()
                    )
                }
            )
        }

        updated = editedInstance.javaOverride?.let { override ->
            updated.copy(javaInstallationId = (override as? JavaOverrideEdit.Specific)?.installationId)
        } ?: updated

        updated = editedInstance.jvmMemoryOptionsEdit?.let { edit ->
            updated.copy(jvmMemoryOptions = (edit as? JvmMemoryOptionsEdit.Custom)?.options)
        } ?: updated

        return updated
    }


    @Composable
    private fun observeInstanceView(): State<ProgressiveOutcome<InstanceError, InstanceView>> =
        produceAndCollectAsProgressiveState(screen.instanceId) {
            observeInstance(screen.instanceId).map { outcome -> outcome.map { it.toView() } }
        }

    @Composable
    private fun observeJavaInstallationsView(): State<ProgressiveOutcome<Nothing, List<JavaInstallation>>> =
        produceAndCollectAsProgressiveState { observeJavaInstallations() }

    @Composable
    private fun observeInstanceLogsView(): State<List<InstanceLogEventView>?> =
        produceAndCollectAsRetainedState(observeInstanceLogs, screen, initial = null) {
            observeInstanceLogs(screen.instanceId).map { list -> list?.map { it.toView() } }
        }

    @Composable
    private fun observeInstalledModsView(): State<List<InstalledContentFileView>?> =
        produceAndCollectAsRetainedState(observeInstalledMods, screen, initial = null) {
            observeInstalledMods(screen.instanceId).map { outcome -> outcome.valueOrNull?.map { it.toView() } }
        }

    @Composable
    private fun observeInstalledResourcePacksView(): State<List<InstalledContentFileView>?> =
        produceAndCollectAsRetainedState(observeInstalledResourcePacks, screen, initial = null) {
            observeInstalledResourcePacks(screen.instanceId).map { outcome -> outcome.valueOrNull?.map { it.toView() } }
        }

    @Composable
    private fun observeInstalledShaderPacksView(): State<List<InstalledContentFileView>?> =
        produceAndCollectAsRetainedState(observeInstalledShaderPacks, screen, initial = null) {
            observeInstalledShaderPacks(screen.instanceId).map { outcome -> outcome.valueOrNull?.map { it.toView() } }
        }

    @Composable
    private fun observeGameVersionListView(): State<ProgressiveOutcome<GameVersionError.InvalidManifest, List<GameVersionView>>> =
        produceAndCollectAsProgressiveState {
            observeGameVersionList().map { res -> res.map { it.toView() } }
        }

    @CircuitInject(InstanceDetailsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: InstanceDetailsScreen, navigator: Navigator): InstanceDetailsPresenter
    }
}
