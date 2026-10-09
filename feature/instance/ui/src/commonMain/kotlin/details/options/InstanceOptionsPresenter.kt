package app.vercors.feature.instance.ui.details.options

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import app.vercors.core.system.OsType
import app.vercors.core.system.SystemService
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.game.domain.usecase.GetRequiredJavaMajorVersionUseCase
import app.vercors.feature.game.domain.usecase.ObserveAvailableLoadersUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.domain.usecase.ObserveLoaderVersionsUseCase
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.usecase.ObserveInstanceUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceUseCase
import app.vercors.feature.instance.ui.details.InstanceView
import app.vercors.feature.instance.ui.details.toView
import app.vercors.feature.instance.ui.observeAvailableLoadersView
import app.vercors.feature.instance.ui.observeGameVersionListView
import app.vercors.feature.instance.ui.observeLoaderVersionsView
import app.vercors.feature.java.domain.usecase.ObserveJavaInstallationsUseCase
import app.vercors.feature.java.domain.usecase.ResolveJavaInstallationUseCase
import app.vercors.shared.domain.model.DetectedNativeLibraries
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.domain.usecase.DetectNativeLibrariesUseCase
import app.vercors.shared.domain.usecase.GetTotalPhysicalMemoryMbUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull
import kotlin.time.Clock

@AssistedInject
class InstanceOptionsPresenter(
    @Assisted private val instanceId: InstanceId,
    private val observeInstance: ObserveInstanceUseCase,
    private val updateInstance: UpdateInstanceUseCase,
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val observeAvailableLoaders: ObserveAvailableLoadersUseCase,
    private val observeLoaderVersions: ObserveLoaderVersionsUseCase,
    private val observeJavaInstallations: ObserveJavaInstallationsUseCase,
    private val getTotalPhysicalMemoryMb: GetTotalPhysicalMemoryMbUseCase,
    private val detectNativeLibraries: DetectNativeLibrariesUseCase,
    private val systemService: SystemService,
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val resolveJavaInstallation: ResolveJavaInstallationUseCase,
    private val getRequiredJavaMajorVersion: GetRequiredJavaMajorVersionUseCase,
    private val globalScope: CoroutineScope,
    private val snackbarService: SnackbarService,
) : Presenter<InstanceOptionsState, InstanceOptionsEvent> {
    @Composable
    override fun present(): Store<InstanceOptionsState, InstanceOptionsEvent> {
        val instance by observeInstanceView()
        val javaInstallations by observeJavaInstallationsView()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val (editedInstance, setEditedInstance) = rememberSaveable { mutableStateOf(InstanceEditState()) }
        val totalMemoryMb = remember { getTotalPhysicalMemoryMb() }
        val showNativeLibraryOptions = systemService.osType == OsType.Linux
        val detectedNativeLibraries by produceState<DetectedNativeLibraries?>(null, showNativeLibraryOptions) {
            if (showNativeLibraryOptions) value = detectNativeLibraries()
        }

        val currentInstance = instance.valueOrNull
        val effectiveGameVersion = editedInstance.gameVersion ?: currentInstance?.gameVersion

        val (requiredJavaMajorVersion, setRequiredJavaMajorVersion) = remember { mutableStateOf<Int?>(null) }
        val (inheritedJavaInstallation, setInheritedJavaInstallation) =
            remember { mutableStateOf<JavaInstallation?>(null) }
        LaunchedEffect(effectiveGameVersion) {
            val majorVersion = effectiveGameVersion?.let { getRequiredJavaMajorVersion(it) }
            setRequiredJavaMajorVersion(majorVersion)
            setInheritedJavaInstallation(majorVersion?.let { resolveJavaInstallation(it, instanceOverrideId = null) })
        }
        val effectiveLoaderType = when (val edit = editedInstance.modLoaderType) {
            is ModLoaderTypeEdit.Loader -> edit.type
            ModLoaderTypeEdit.Vanilla -> null
            null -> currentInstance?.loaderType
        }

        val gameVersionList by observeGameVersionListView(observeGameVersionList)
        val loaderList by observeAvailableLoadersView(observeAvailableLoaders, effectiveGameVersion)
        val loaderVersionList by observeLoaderVersionsView(
            observeLoaderVersions,
            effectiveGameVersion,
            effectiveLoaderType
        )

        LaunchedEffect(editedInstance.modLoaderType, editedInstance.modLoaderVersion, loaderVersionList) {
            val versions = loaderVersionList.valueOrNull
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

        return InstanceOptionsState(
            editedInstance = editedInstance,
            gameVersionList = gameVersionList.valueOrNull ?: emptyList(),
            loaderList = loaderList.valueOrNull ?: emptyList(),
            loaderVersionList = loaderVersionList.valueOrNull ?: emptyList(),
            availableJavaInstallations = javaInstallations.valueOrNull ?: emptyList(),
            totalMemoryMb = totalMemoryMb,
            showNativeLibraryOptions = showNativeLibraryOptions,
            detectedGlfwPath = detectedNativeLibraries?.glfwPath,
            detectedOpenAlPath = detectedNativeLibraries?.openAlPath,
            globalJvmMemoryOptions = configuration.valueOrNull?.jvmMemoryOptions ?: JvmMemoryOptions(),
            globalNativeLibraryOptions = configuration.valueOrNull?.nativeLibraryOptions ?: NativeLibraryOptions(),
            inheritedJavaInstallation = inheritedJavaInstallation,
            requiredJavaMajorVersion = requiredJavaMajorVersion,
            persistedInstance = currentInstance,
        ) + { event ->
            handleEvent(
                event = event,
                editedInstance = editedInstance,
                setEditedInstance = setEditedInstance,
            )
        }
    }

    private fun handleEvent(
        event: InstanceOptionsEvent,
        editedInstance: InstanceEditState,
        setEditedInstance: (InstanceEditState) -> Unit
    ) {
        when (event) {
            is InstanceOptionsEvent.OnInstanceEditChange -> setEditedInstance(event.update(editedInstance))
            is InstanceOptionsEvent.OnLoaderChange -> handleLoaderChange(event, editedInstance, setEditedInstance)
            is InstanceOptionsEvent.OnLoaderVersionChange ->
                setEditedInstance(editedInstance.copy(modLoaderVersion = event.version))

            is InstanceOptionsEvent.OnJavaOverrideChange -> setEditedInstance(
                editedInstance.copy(
                    javaOverride = event.installationId?.let(JavaOverrideEdit::Specific)
                        ?: JavaOverrideEdit.UseDefault
                )
            )

            InstanceOptionsEvent.OnCancelEditClick -> setEditedInstance(InstanceEditState())
            InstanceOptionsEvent.OnSaveClick -> handleSaveInstance(editedInstance, setEditedInstance)
        }
    }

    private fun handleLoaderChange(
        event: InstanceOptionsEvent.OnLoaderChange,
        editedInstance: InstanceEditState,
        setEditedInstance: (InstanceEditState) -> Unit
    ) = setEditedInstance(
        editedInstance.copy(
            modLoaderType = event.loader?.let(ModLoaderTypeEdit::Loader) ?: ModLoaderTypeEdit.Vanilla,
            // Reset so the recommended-version effect can pick one that's valid for the new type
            modLoaderVersion = null
        )
    )

    private fun handleSaveInstance(editedInstance: InstanceEditState, setEditedInstance: (InstanceEditState) -> Unit) =
        globalScope.launch {
            either {
                updateInstance(instanceId) { it.withEdits(editedInstance).copy(updatedAt = Clock.System.now()) }
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
            updated.copy(jvmMemoryOptions = edit)
        } ?: updated

        updated = editedInstance.nativeLibraryOptionsEdit?.let { edit ->
            updated.copy(nativeLibraryOptions = edit)
        } ?: updated

        return updated
    }

    @Composable
    private fun observeInstanceView(): State<ProgressiveOutcome<InstanceError, InstanceView>> =
        produceAndCollectAsProgressiveState(instanceId) {
            observeInstance(instanceId).map { outcome -> outcome.map { it.toView() } }
        }

    @Composable
    private fun observeJavaInstallationsView(): State<ProgressiveOutcome<Nothing, List<JavaInstallation>>> =
        produceAndCollectAsProgressiveState { observeJavaInstallations() }

    @AssistedFactory
    fun interface Factory {
        fun create(instanceId: InstanceId): InstanceOptionsPresenter
    }
}
