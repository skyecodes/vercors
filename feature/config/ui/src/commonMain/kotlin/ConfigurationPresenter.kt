package app.vercors.feature.config.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.feature.theme.domain.model.ThemeDefinition
import app.vercors.feature.theme.domain.model.ThemeNamedColor
import app.vercors.feature.theme.domain.usecase.ObserveThemesUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.collectAsRetainedState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.value
import opensavvy.state.progressive.valueOrNull

@CircuitInject(ConfigurationScreen::class, AppScope::class)
@Inject
class ConfigurationPresenter(
    private val observeThemes: ObserveThemesUseCase,
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
) : Presenter<ConfigurationState, ConfigurationEvent> {
    @Composable
    override fun present(): Store<ConfigurationState, ConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val themeList by observeThemeViewList()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val cfg = configuration.valueOrNull
        val selectedTheme = themeList.find { it.id == cfg?.selectedThemeId }
        val selectedAccent = selectedTheme?.accents?.find { it.id == cfg?.selectedAccent }

        return ConfigurationState(
            selectedTheme = selectedTheme,
            themeList = themeList,
            selectedAccent = selectedAccent,
            animations = cfg?.animations,
        ) + {
            when (it) {
                is ConfigurationEvent.SelectTheme -> presenterScope.launch {
                    selectTheme(themeList.first { t -> t.id == it.themeId })
                }

                is ConfigurationEvent.SelectAccent -> presenterScope.launch {
                    updateConfiguration { copy(selectedAccent = it.accentId) }
                }

                ConfigurationEvent.ToggleAnimations -> presenterScope.launch {
                    updateConfiguration { copy(animations = !animations) }
                }
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

private fun ThemeDefinition.toView() = ThemeView(
    id = id,
    displayName = displayName,
    accents = accents.map { it.toView() },
    defaultAccent = accents.first { it.id == defaultAccent }.toView()
)

private fun ThemeNamedColor.toView() = ThemeNamedColorView(
    id = id,
    name = name,
    color = Color(color.value)
)
