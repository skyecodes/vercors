package app.vercors.feature.config.ui.advanced

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull
import kotlin.time.Duration.Companion.seconds

@Inject
class AdvancedConfigurationPresenter(
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
) : Presenter<AdvancedConfigurationState, AdvancedConfigurationEvent> {
    @Composable
    override fun present(): Store<AdvancedConfigurationState, AdvancedConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val cfg = configuration.valueOrNull

        return AdvancedConfigurationState(
            metaApiUrl = cfg?.metaApiUrl,
            networkTimeoutSeconds = cfg?.networkTimeout?.inWholeSeconds?.toInt(),
            parallelDownloads = cfg?.parallelDownloads,
            parallelFilesystemOperations = cfg?.parallelFilesystemOperations,
        ) + { event ->
            handleEvent(
                event = event,
                presenterScope = presenterScope,
            )
        }
    }

    private fun handleEvent(event: AdvancedConfigurationEvent, presenterScope: CoroutineScope) {
        when (event) {
            is AdvancedConfigurationEvent.OnMetaApiUrlChange -> presenterScope.launch {
                updateConfiguration { copy(metaApiUrl = event.value) }
            }

            is AdvancedConfigurationEvent.OnNetworkTimeoutChange -> presenterScope.launch {
                updateConfiguration { copy(networkTimeout = event.value.seconds) }
            }

            is AdvancedConfigurationEvent.OnParallelDownloadsChange -> presenterScope.launch {
                updateConfiguration { copy(parallelDownloads = event.value) }
            }

            is AdvancedConfigurationEvent.OnParallelFilesystemOperationsChange -> presenterScope.launch {
                updateConfiguration { copy(parallelFilesystemOperations = event.value) }
            }
        }
    }
}
