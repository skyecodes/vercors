package app.vercors.feature.config.ui.nativelibraries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import app.vercors.core.system.OsType
import app.vercors.core.system.SystemService
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.shared.domain.model.DetectedNativeLibraries
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.domain.usecase.DetectNativeLibrariesUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull

@Inject
class NativeLibraryConfigurationPresenter(
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
    private val detectNativeLibraries: DetectNativeLibrariesUseCase,
    private val systemService: SystemService,
) : Presenter<NativeLibraryConfigurationState, NativeLibraryConfigurationEvent> {
    @Composable
    override fun present(): Store<NativeLibraryConfigurationState, NativeLibraryConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val supported = systemService.osType == OsType.Linux
        val detectedNativeLibraries by produceState<DetectedNativeLibraries?>(null, supported) {
            if (supported) value = detectNativeLibraries()
        }

        return NativeLibraryConfigurationState(
            supported = supported,
            nativeLibraryOptions = configuration.valueOrNull?.nativeLibraryOptions ?: NativeLibraryOptions(),
            detectedGlfwPath = detectedNativeLibraries?.glfwPath,
            detectedOpenAlPath = detectedNativeLibraries?.openAlPath,
        ) + { event ->
            handleEvent(
                event = event,
                presenterScope = presenterScope,
            )
        }
    }

    private fun handleEvent(event: NativeLibraryConfigurationEvent, presenterScope: CoroutineScope) {
        when (event) {
            is NativeLibraryConfigurationEvent.OnUseSystemGlfwToggle -> presenterScope.launch {
                updateConfiguration {
                    copy(nativeLibraryOptions = nativeLibraryOptions.copy(useSystemGlfw = event.checked))
                }
            }

            is NativeLibraryConfigurationEvent.OnGlfwPathChange -> presenterScope.launch {
                updateConfiguration { copy(nativeLibraryOptions = nativeLibraryOptions.copy(glfwPath = event.value)) }
            }

            is NativeLibraryConfigurationEvent.OnUseSystemOpenAlToggle -> presenterScope.launch {
                updateConfiguration {
                    copy(nativeLibraryOptions = nativeLibraryOptions.copy(useSystemOpenAl = event.checked))
                }
            }

            is NativeLibraryConfigurationEvent.OnOpenAlPathChange -> presenterScope.launch {
                updateConfiguration { copy(nativeLibraryOptions = nativeLibraryOptions.copy(openAlPath = event.value)) }
            }
        }
    }
}
