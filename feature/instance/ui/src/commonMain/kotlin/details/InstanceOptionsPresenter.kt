package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import app.vercors.core.system.OsType
import app.vercors.core.system.SystemService
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.usecase.*
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.game.ui.toView
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.usecase.ObserveInstanceUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceUseCase
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.domain.model.NativeLibraryType
import app.vercors.shared.domain.usecase.DetectNativeLibraryPathUseCase
import app.vercors.shared.domain.usecase.GetTotalPhysicalMemoryUseCase
import app.vercors.shared.ui.*
import arrow.core.raise.context.either
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull
import kotlin.time.Clock

@Immutable
data class InstanceOptionsState(
    val editedInstance: InstanceEditState,
    val gameVersionList: List<GameVersionView>,
    val loaderList: List<ModLoaderType>,
    val loaderVersionList: List<LoaderVersion>,
    val availableJavaInstallations: List<JavaInstallation>,
    val totalMemoryMb: Int,
    val showNativeLibraryOptions: Boolean,
    val detectedGlfwPath: String?,
    val detectedOpenAlPath: String?,
    val globalJvmMemoryOptions: JvmMemoryOptions,
    val globalNativeLibraryOptions: NativeLibraryOptions,
    val inheritedJavaInstallation: JavaInstallation?,
    val requiredJavaMajorVersion: Int?,
    private val persistedInstance: InstanceView?,
) {
    val isNameEdited = persistedInstance?.let {
        editedInstance.name != null && editedInstance.name != it.name
    } ?: false
    val isGameVersionEdited = persistedInstance?.let {
        editedInstance.gameVersion != null && editedInstance.gameVersion != it.gameVersion
    } ?: false
    val isLoaderTypeEdited = persistedInstance?.let {
        val current: ModLoaderTypeEdit = it.loaderType?.let(ModLoaderTypeEdit::Loader) ?: ModLoaderTypeEdit.Vanilla
        editedInstance.modLoaderType != null && editedInstance.modLoaderType != current
    } ?: false
    val isLoaderVersionEdited = persistedInstance?.let {
        editedInstance.modLoaderVersion != null && editedInstance.modLoaderVersion != it.loaderVersion
    } ?: false
    val isModLoaderEdited = isLoaderTypeEdited || isLoaderVersionEdited
    val isJavaOverrideEdited = persistedInstance?.let {
        val current: JavaOverrideEdit =
            it.javaInstallationId?.let(JavaOverrideEdit::Specific) ?: JavaOverrideEdit.UseDefault
        editedInstance.javaOverride != null && editedInstance.javaOverride != current
    } ?: false
    val isJvmMemoryOptionsEdited = persistedInstance?.let {
        editedInstance.jvmMemoryOptionsEdit != null && editedInstance.jvmMemoryOptionsEdit != it.jvmMemoryOptions
    } ?: false
    val isNativeLibraryOptionsEdited = persistedInstance?.let {
        editedInstance.nativeLibraryOptionsEdit != null && editedInstance.nativeLibraryOptionsEdit != it.nativeLibraryOptions
    } ?: false
    val isInstanceEdited: Boolean =
        isNameEdited || isGameVersionEdited || isModLoaderEdited || isJavaOverrideEdited ||
                isJvmMemoryOptionsEdited || isNativeLibraryOptionsEdited
}

@Immutable
data class InstanceEditState(
    val name: String? = null,
    val gameVersion: String? = null,
    val modLoaderType: ModLoaderTypeEdit? = null,
    val modLoaderVersion: String? = null,
    val javaOverride: JavaOverrideEdit? = null,
    val jvmMemoryOptionsEdit: JvmMemoryOptions? = null,
    val nativeLibraryOptionsEdit: NativeLibraryOptions? = null,
)

@Immutable
sealed interface ModLoaderTypeEdit {
    data object Vanilla : ModLoaderTypeEdit

    @JvmInline
    value class Loader(val type: ModLoaderType) : ModLoaderTypeEdit
}

@Immutable
sealed interface JavaOverrideEdit {
    data object UseDefault : JavaOverrideEdit

    @JvmInline
    value class Specific(val installationId: String) : JavaOverrideEdit
}

@Immutable
sealed interface InstanceOptionsEvent : Event {
    data class UpdateInstance(val update: (InstanceEditState) -> InstanceEditState) : InstanceOptionsEvent
    data class OnLoaderChange(val loader: ModLoaderType?) : InstanceOptionsEvent
    data class OnLoaderVersionChange(val version: String) : InstanceOptionsEvent
    data class OnJavaOverrideChange(val installationId: String?) : InstanceOptionsEvent
    data object SaveInstance : InstanceOptionsEvent
    data object CancelEdit : InstanceOptionsEvent
}

@AssistedInject
class InstanceOptionsPresenter(
    @Assisted private val instanceId: InstanceId,
    private val observeInstance: ObserveInstanceUseCase,
    private val updateInstance: UpdateInstanceUseCase,
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val observeAvailableLoaders: ObserveAvailableLoadersUseCase,
    private val observeLoaderVersions: ObserveLoaderVersionsUseCase,
    private val observeJavaInstallations: ObserveJavaInstallationsUseCase,
    private val getTotalPhysicalMemory: GetTotalPhysicalMemoryUseCase,
    private val detectNativeLibraryPath: DetectNativeLibraryPathUseCase,
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
        val totalMemoryMb = remember {
            ((getTotalPhysicalMemory() ?: (16384L * 1024 * 1024)) / (1024 * 1024)).toInt()
        }
        val showNativeLibraryOptions = systemService.osType == OsType.Linux
        var detectedGlfwPath by remember { mutableStateOf<String?>(null) }
        var detectedOpenAlPath by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(showNativeLibraryOptions) {
            if (showNativeLibraryOptions) {
                detectedGlfwPath = detectNativeLibraryPath(NativeLibraryType.Glfw)
                detectedOpenAlPath = detectNativeLibraryPath(NativeLibraryType.OpenAl)
            }
        }

        val currentInstance = instance.valueOrNull
        val effectiveGameVersion = editedInstance.gameVersion ?: currentInstance?.gameVersion

        var requiredJavaMajorVersion by remember { mutableStateOf<Int?>(null) }
        var inheritedJavaInstallation by remember { mutableStateOf<JavaInstallation?>(null) }
        LaunchedEffect(effectiveGameVersion) {
            val majorVersion = effectiveGameVersion?.let { getRequiredJavaMajorVersion(it) }
            requiredJavaMajorVersion = majorVersion
            inheritedJavaInstallation = majorVersion?.let { resolveJavaInstallation(it, instanceOverrideId = null) }
        }
        val effectiveLoaderType = when (val edit = editedInstance.modLoaderType) {
            is ModLoaderTypeEdit.Loader -> edit.type
            ModLoaderTypeEdit.Vanilla -> null
            null -> currentInstance?.loaderType
        }

        val gameVersionList by observeGameVersionListView()
        val loaderList by observeAvailableLoadersView(effectiveGameVersion)
        val loaderVersionList by observeLoaderVersionsView(effectiveGameVersion, effectiveLoaderType)

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
            detectedGlfwPath = detectedGlfwPath,
            detectedOpenAlPath = detectedOpenAlPath,
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
            is InstanceOptionsEvent.UpdateInstance -> setEditedInstance(event.update(editedInstance))
            is InstanceOptionsEvent.OnLoaderChange -> handleLoaderChange(event, editedInstance, setEditedInstance)
            is InstanceOptionsEvent.OnLoaderVersionChange ->
                setEditedInstance(editedInstance.copy(modLoaderVersion = event.version))

            is InstanceOptionsEvent.OnJavaOverrideChange -> setEditedInstance(
                editedInstance.copy(
                    javaOverride = event.installationId?.let(JavaOverrideEdit::Specific)
                        ?: JavaOverrideEdit.UseDefault
                )
            )

            InstanceOptionsEvent.CancelEdit -> setEditedInstance(InstanceEditState())
            InstanceOptionsEvent.SaveInstance -> handleSaveInstance(editedInstance, setEditedInstance)
        }
    }

    private fun handleLoaderChange(
        event: InstanceOptionsEvent.OnLoaderChange,
        editedInstance: InstanceEditState,
        setEditedInstance: (InstanceEditState) -> Unit
    ) = setEditedInstance(
        editedInstance.copy(
            modLoaderType = event.loader?.let(ModLoaderTypeEdit::Loader) ?: ModLoaderTypeEdit.Vanilla,
            // Reset so the recommended-version effect can pick one that's valid for the new type.
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

    @Composable
    private fun observeGameVersionListView(): State<ProgressiveOutcome<GameVersionError.InvalidManifest, List<GameVersionView>>> =
        produceAndCollectAsProgressiveState {
            observeGameVersionList().map { res -> res.map { it.toView() } }
        }

    @Composable
    private fun observeAvailableLoadersView(gameVersion: String?): State<ProgressiveOutcome<LoaderError, List<ModLoaderType>>> =
        produceAndCollectAsProgressiveState(gameVersion) {
            gameVersion?.let { observeAvailableLoaders(it) } ?: flowOf(ProgressiveOutcome.Incomplete())
        }

    @Composable
    private fun observeLoaderVersionsView(
        gameVersion: String?,
        loader: ModLoaderType?
    ): State<ProgressiveOutcome<LoaderError, List<LoaderVersion>>> =
        produceAndCollectAsProgressiveState(gameVersion, loader) {
            if (gameVersion != null && loader != null) observeLoaderVersions(gameVersion, loader)
            else flowOf(ProgressiveOutcome.Incomplete())
        }

    @AssistedFactory
    fun interface Factory {
        fun create(instanceId: InstanceId): InstanceOptionsPresenter
    }
}
