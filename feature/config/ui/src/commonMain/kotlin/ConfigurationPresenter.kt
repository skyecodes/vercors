package app.vercors.feature.config.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.model.isUpdateAvailable
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.ui.about.AboutConfigurationPresenter
import app.vercors.feature.config.ui.advanced.AdvancedConfigurationPresenter
import app.vercors.feature.config.ui.appearance.AppearanceConfigurationPresenter
import app.vercors.feature.config.ui.gameoptions.GameOptionsConfigurationPresenter
import app.vercors.feature.config.ui.general.GeneralConfigurationPresenter
import app.vercors.feature.config.ui.java.JavaConfigurationPresenter
import app.vercors.feature.config.ui.nativelibraries.NativeLibraryConfigurationPresenter
import app.vercors.feature.config.ui.storage.StorageConfigurationPresenter
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import opensavvy.state.progressive.valueOrNull

@CircuitInject(ConfigurationScreen::class, AppScope::class)
@Inject
class ConfigurationPresenter(
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val generalPresenter: GeneralConfigurationPresenter,
    private val advancedPresenter: AdvancedConfigurationPresenter,
    private val appearancePresenter: AppearanceConfigurationPresenter,
    private val javaPresenter: JavaConfigurationPresenter,
    private val gameOptionsPresenter: GameOptionsConfigurationPresenter,
    private val nativeLibraryPresenter: NativeLibraryConfigurationPresenter,
    private val storagePresenter: StorageConfigurationPresenter,
    private val aboutPresenter: AboutConfigurationPresenter,
) : Presenter<ConfigurationState, ConfigurationEvent> {
    @Composable
    override fun present(): Store<ConfigurationState, ConfigurationEvent> {
        val (selectedTab, setSelectedTab) = rememberSaveable { mutableStateOf(ConfigurationTab.General) }
        val configuration by observeConfiguration().collectProgressiveAsState()
        val nativeLibrariesContent = nativeLibraryPresenter.present()
        val aboutContent = aboutPresenter.present()

        return ConfigurationState(
            selectedTab = selectedTab,
            horizontalTabs = configuration.valueOrNull?.horizontalTabs,
            showNativeLibrariesTab = nativeLibrariesContent.state.supported,
            updateAvailable = aboutContent.state.launcherUpdate.isUpdateAvailable,
            generalContent = generalPresenter.present(),
            advancedContent = advancedPresenter.present(),
            appearanceContent = appearancePresenter.present(),
            javaContent = javaPresenter.present(),
            gameOptionsContent = gameOptionsPresenter.present(),
            nativeLibrariesContent = nativeLibrariesContent,
            storageContent = storagePresenter.present(),
            aboutContent = aboutContent,
        ) + { event ->
            handleEvent(
                event = event,
                setSelectedTab = setSelectedTab,
            )
        }
    }

    private fun handleEvent(event: ConfigurationEvent, setSelectedTab: (ConfigurationTab) -> Unit) {
        when (event) {
            is ConfigurationEvent.OnTabClick -> setSelectedTab(event.tab)
        }
    }
}
