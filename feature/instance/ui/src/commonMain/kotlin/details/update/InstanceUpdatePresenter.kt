package app.vercors.feature.instance.ui.details.update

import androidx.compose.runtime.*
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.usecase.HasActiveInstanceJobUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceJobUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceStatusUseCase
import app.vercors.feature.instance.ui.details.*
import app.vercors.feature.instance.ui.install.LocalPackUpdateLauncher
import app.vercors.feature.instance.ui.install.notifyModpackInstallResult
import app.vercors.feature.instance.ui.message
import app.vercors.feature.project.domain.model.update.ModpackUpdate
import app.vercors.feature.project.domain.usecase.*
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull

private val logger = KotlinLogging.logger {}

@AssistedInject
class InstanceUpdatePresenter(
    @Assisted private val instanceId: InstanceId,
    private val globalScope: CoroutineScope,
    private val observeInstance: ObserveInstanceUseCase,
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val observeInstanceUpdates: ObserveInstanceUpdatesUseCase,
    private val checkInstanceUpdates: CheckInstanceUpdatesUseCase,
    private val shouldAutoCheckInstanceUpdates: ShouldAutoCheckInstanceUpdatesUseCase,
    private val updateModpack: UpdateModpackUseCase,
    private val parseLocalPack: ParseLocalPackUseCase,
    private val getInstalledPack: GetInstalledPackUseCase,
    private val localPackUpdateLauncher: LocalPackUpdateLauncher,
    private val hasActiveInstanceJob: HasActiveInstanceJobUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val snackbarService: SnackbarService,
) : Presenter<InstanceUpdateState, InstanceUpdateEvent> {
    @Composable
    override fun present(): Store<InstanceUpdateState, InstanceUpdateEvent> {
        val instance by observeInstanceView(observeInstance, instanceId)
        val configuration by observeConfiguration().collectProgressiveAsState()
        val modpackUpdate by remember(instanceId) {
            observeInstanceUpdates(instanceId).map { it?.modpack }
        }.collectAsState(null)
        val (promptModpackUpdate, setPromptModpackUpdate) = remember { mutableStateOf(false) }
        val (localPackUpdatePrompt, setLocalPackUpdatePrompt) = remember {
            mutableStateOf<LocalPackUpdatePrompt?>(null)
        }
        val currentInstance = instance.valueOrNull
        AutoCheckUpdatesEffect(currentInstance, configuration.valueOrNull?.checkForInstanceUpdates)

        return InstanceUpdateState(
            modpackUpdate = currentInstance?.let { modpackUpdate?.toView(it.gameVersion) },
            promptModpackUpdate = promptModpackUpdate,
            localPackUpdatePrompt = localPackUpdatePrompt,
        ) + { event ->
            handleEvent(
                event = event,
                currentInstance = currentInstance,
                modpackUpdate = modpackUpdate,
                setPromptModpackUpdate = setPromptModpackUpdate,
                localPackUpdatePrompt = localPackUpdatePrompt,
                setLocalPackUpdatePrompt = setLocalPackUpdatePrompt,
            )
        }
    }

    // Automatic checks fail silently: they run on every open and a snackbar each time would be noise
    @Composable
    private fun AutoCheckUpdatesEffect(instance: InstanceView?, globalCheckForUpdates: Boolean?) {
        val ready = instance != null && globalCheckForUpdates != null
        LaunchedEffect(instanceId, ready) {
            if (instance == null || globalCheckForUpdates == null) return@LaunchedEffect
            if (instance.status is InstanceStatus.Installing) return@LaunchedEffect
            if (!shouldAutoCheckInstanceUpdates(instanceId, instance.checkForUpdates, globalCheckForUpdates)) {
                return@LaunchedEffect
            }
            globalScope.launch {
                either { checkInstanceUpdates(instanceId, identifyUnknown = false) }
                    .onLeft { error -> logger.warn { "Automatic update check of $instanceId failed: $error" } }
            }
        }
    }

    private fun handleEvent(
        event: InstanceUpdateEvent,
        currentInstance: InstanceView?,
        modpackUpdate: ModpackUpdate?,
        setPromptModpackUpdate: (Boolean) -> Unit,
        localPackUpdatePrompt: LocalPackUpdatePrompt?,
        setLocalPackUpdatePrompt: (LocalPackUpdatePrompt?) -> Unit,
    ) {
        when (event) {
            InstanceUpdateEvent.OnUpdateModpackClick -> setPromptModpackUpdate(true)
            is InstanceUpdateEvent.OnConfirmModpackUpdate -> handleConfirmModpackUpdate(
                event = event,
                currentInstance = currentInstance,
                modpackUpdate = modpackUpdate,
                setPromptModpackUpdate = setPromptModpackUpdate,
            )

            is InstanceUpdateEvent.OnUpdateFromFileSelected ->
                handleUpdateFromFileSelected(event, setLocalPackUpdatePrompt)

            is InstanceUpdateEvent.OnConfirmLocalPackUpdate -> handleConfirmLocalPackUpdate(
                event = event,
                currentInstance = currentInstance,
                localPackUpdatePrompt = localPackUpdatePrompt,
                setLocalPackUpdatePrompt = setLocalPackUpdatePrompt,
            )
        }
    }

    private fun handleUpdateFromFileSelected(
        event: InstanceUpdateEvent.OnUpdateFromFileSelected,
        setLocalPackUpdatePrompt: (LocalPackUpdatePrompt?) -> Unit,
    ) {
        globalScope.launch {
            either { parseLocalPack(event.sourcePath) }
                .onLeft { error -> snackbarService.error { error.message } }
                .onRight { pack -> setLocalPackUpdatePrompt(pack.toUpdatePrompt(getInstalledPack(instanceId))) }
        }
    }

    private fun handleConfirmLocalPackUpdate(
        event: InstanceUpdateEvent.OnConfirmLocalPackUpdate,
        currentInstance: InstanceView?,
        localPackUpdatePrompt: LocalPackUpdatePrompt?,
        setLocalPackUpdatePrompt: (LocalPackUpdatePrompt?) -> Unit,
    ) {
        setLocalPackUpdatePrompt(null)
        if (!event.result || currentInstance == null || localPackUpdatePrompt == null) return
        if (hasActiveInstanceJob(instanceId)) return
        localPackUpdateLauncher.launch(instanceId, currentInstance.name, localPackUpdatePrompt.pack)
    }

    private fun handleConfirmModpackUpdate(
        event: InstanceUpdateEvent.OnConfirmModpackUpdate,
        currentInstance: InstanceView?,
        modpackUpdate: ModpackUpdate?,
        setPromptModpackUpdate: (Boolean) -> Unit,
    ) {
        setPromptModpackUpdate(false)
        if (!event.result || currentInstance == null || modpackUpdate == null) return
        if (hasActiveInstanceJob(instanceId)) return

        val job = globalScope.launch {
            val ok = either {
                updateModpack(
                    instanceId = instanceId,
                    update = modpackUpdate,
                    onManualDownloadError = { error -> snackbarService.error { error.message } },
                )
            }.onLeft { error -> snackbarService.error { error.message } }.getOrNull() ?: return@launch
            updateInstanceStatus(instanceId, if (ok) InstanceStatus.Idle else InstanceStatus.Failed.InstallFailed)
            notifyModpackInstallResult(currentInstance.name, ok)
        }
        updateInstanceJob(instanceId, job)
        job.invokeOnCompletion { updateInstanceJob(instanceId, null) }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(instanceId: InstanceId): InstanceUpdatePresenter
    }
}
