package app.vercors.feature.config.ui.appearance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.feature.theme.domain.usecase.ObserveThemesUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.retained.collectAsRetainedState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.value
import opensavvy.state.progressive.valueOrNull

@Inject
class AppearanceConfigurationPresenter(
    private val observeThemes: ObserveThemesUseCase,
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
) : Presenter<AppearanceConfigurationState, AppearanceConfigurationEvent> {
    @Composable
    override fun present(): Store<AppearanceConfigurationState, AppearanceConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val themeList by observeThemeViewList()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val cfg = configuration.valueOrNull
        val selectedTheme = themeList.find { it.id == cfg?.selectedThemeId }

        return AppearanceConfigurationState(
            selectedTheme = selectedTheme,
            themeList = themeList,
            selectedAccent = selectedTheme?.accents?.find { it.id == cfg?.selectedAccent },
            animations = cfg?.animations,
            uiScalePercent = cfg?.uiScalePercent,
            horizontalTabs = cfg?.horizontalTabs,
        ) + { event ->
            handleEvent(
                event = event,
                presenterScope = presenterScope,
                themeList = themeList,
            )
        }
    }

    private fun handleEvent(
        event: AppearanceConfigurationEvent,
        presenterScope: CoroutineScope,
        themeList: List<ThemeView>,
    ) {
        when (event) {
            is AppearanceConfigurationEvent.OnThemeChange -> presenterScope.launch {
                selectTheme(themeList.first { t -> t.id == event.themeId })
            }

            is AppearanceConfigurationEvent.OnAccentChange -> presenterScope.launch {
                updateConfiguration { copy(selectedAccent = event.accentId) }
            }

            AppearanceConfigurationEvent.OnAnimationsToggle -> presenterScope.launch {
                updateConfiguration { copy(animations = !animations) }
            }

            AppearanceConfigurationEvent.OnHorizontalTabsToggle -> presenterScope.launch {
                updateConfiguration { copy(horizontalTabs = !horizontalTabs) }
            }

            is AppearanceConfigurationEvent.OnUiScalePercentChange -> presenterScope.launch {
                updateConfiguration { copy(uiScalePercent = event.value) }
            }
        }
    }

    @Composable
    private fun observeThemeViewList() = observeThemes()
        .map { it.value.themes.map { theme -> theme.toView() } }
        .collectAsRetainedState(emptyList())

    private suspend fun selectTheme(theme: ThemeView) {
        updateConfiguration {
            copy(
                selectedThemeId = theme.id,
                selectedAccent = theme.accents.find { it.id == selectedAccent }?.id ?: theme.defaultAccent.id
            )
        }
    }
}
