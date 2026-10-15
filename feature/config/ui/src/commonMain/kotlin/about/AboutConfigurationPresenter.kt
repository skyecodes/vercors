package app.vercors.feature.config.ui.about

import androidx.compose.runtime.*
import app.vercors.core.domain.AppConstants
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.model.UpdateChannel
import app.vercors.feature.config.domain.usecase.*
import app.vercors.feature.config.ui.collectHasRunningInstance
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull

@Inject
class AboutConfigurationPresenter(
    private val globalScope: CoroutineScope,
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val observeLauncherUpdateState: ObserveLauncherUpdateStateUseCase,
    private val checkForLauncherUpdate: CheckForLauncherUpdateUseCase,
    private val downloadLauncherUpdate: DownloadLauncherUpdateUseCase,
    private val installLauncherUpdate: InstallLauncherUpdateUseCase,
    @Named(AppConstants.APP_NAME_KEY) private val appName: String,
    @Named(AppConstants.APP_VERSION_KEY) private val appVersion: String,
) : Presenter<AboutConfigurationState, AboutConfigurationEvent> {
    @Composable
    override fun present(): Store<AboutConfigurationState, AboutConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val hasRunningInstance = observeInstanceList.collectHasRunningInstance()
        val launcherUpdate by observeLauncherUpdateState().collectAsState()
        val (promptRestartForUpdate, setPromptRestartForUpdate) = remember { mutableStateOf(false) }
        val cfg = configuration.valueOrNull

        return AboutConfigurationState(
            appName = appName,
            appVersion = appVersion,
            checkForUpdatesOnStart = cfg?.checkForUpdatesOnStart,
            updateChannel = cfg?.let { it.updateChannel ?: UpdateChannel.fromVersion(appVersion) },
            launcherUpdate = launcherUpdate,
            promptRestartForUpdate = promptRestartForUpdate,
        ) + { event ->
            handleEvent(
                event = event,
                presenterScope = presenterScope,
                hasRunningInstance = hasRunningInstance,
                setPromptRestartForUpdate = setPromptRestartForUpdate,
            )
        }
    }

    private fun handleEvent(
        event: AboutConfigurationEvent,
        presenterScope: CoroutineScope,
        hasRunningInstance: Boolean,
        setPromptRestartForUpdate: (Boolean) -> Unit,
    ) {
        when (event) {
            AboutConfigurationEvent.OnCheckForUpdatesOnStartToggle -> presenterScope.launch {
                updateConfiguration { copy(checkForUpdatesOnStart = !checkForUpdatesOnStart) }
            }

            is AboutConfigurationEvent.OnUpdateChannelChange -> handleUpdateChannelChange(event.value)

            AboutConfigurationEvent.OnCheckForUpdatesClick -> globalScope.launch { checkForLauncherUpdate() }
            AboutConfigurationEvent.OnUpdateClick -> globalScope.launch { downloadLauncherUpdate() }

            AboutConfigurationEvent.OnRestartToUpdateClick ->
                if (hasRunningInstance) setPromptRestartForUpdate(true)
                else globalScope.launch { installLauncherUpdate() }

            is AboutConfigurationEvent.OnConfirmRestartForUpdate -> {
                setPromptRestartForUpdate(false)
                if (event.result) globalScope.launch { installLauncherUpdate() }
            }
        }
    }

    private fun handleUpdateChannelChange(channel: UpdateChannel) {
        globalScope.launch {
            updateConfiguration { copy(updateChannel = channel) }
            checkForLauncherUpdate()
        }
    }
}
