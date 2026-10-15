package app.vercors.feature.config.ui.general

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.core.ui.state.produceProgressiveState
import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.feature.project.domain.model.PlatformError
import app.vercors.feature.project.domain.model.ProjectPlatform
import app.vercors.feature.project.domain.usecase.GetPlatformListUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.valueOrNull

@Inject
class GeneralConfigurationPresenter(
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
    private val getPlatformList: GetPlatformListUseCase,
) : Presenter<GeneralConfigurationState, GeneralConfigurationEvent> {
    @Composable
    override fun present(): Store<GeneralConfigurationState, GeneralConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val platformList by observePlatformList()
        val cfg = configuration.valueOrNull

        return GeneralConfigurationState(
            preferredPlatform = cfg?.preferredPlatform,
            platformList = platformList.valueOrNull ?: emptyList(),
            startupScreen = cfg?.startupScreen,
            checkForInstanceUpdates = cfg?.checkForInstanceUpdates,
            homeCategories = cfg?.homeCategories ?: HomeCategoryConfig.entries,
        ) + { event ->
            handleEvent(
                event = event,
                presenterScope = presenterScope,
            )
        }
    }

    private fun handleEvent(event: GeneralConfigurationEvent, presenterScope: CoroutineScope) {
        when (event) {
            is GeneralConfigurationEvent.OnPreferredPlatformChange -> presenterScope.launch {
                updateConfiguration { copy(preferredPlatform = event.value) }
            }

            is GeneralConfigurationEvent.OnStartupScreenChange -> presenterScope.launch {
                updateConfiguration { copy(startupScreen = event.value) }
            }

            GeneralConfigurationEvent.OnCheckForInstanceUpdatesToggle -> presenterScope.launch {
                updateConfiguration { copy(checkForInstanceUpdates = !checkForInstanceUpdates) }
            }

            is GeneralConfigurationEvent.OnHomeCategoriesChange -> presenterScope.launch {
                updateConfiguration { copy(homeCategories = event.value) }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    private fun observePlatformList(): State<ProgressiveOutcome<PlatformError, List<ProjectPlatform>>> =
        produceProgressiveState {
            value = progressive { getPlatformList() }
        }
}
