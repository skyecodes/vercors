package app.vercors.launcher.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.feature.theme.domain.usecase.ObserveThemesUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull

private val logger = KotlinLogging.logger {}

@AssistedInject
class AppPresenter(
    @Assisted private val screen: AppScreen,
    private val backgroundScope: CoroutineScope,
    private val observeGuiConfiguration: ObserveConfigurationUseCase,
    private val updateGuiConfiguration: UpdateConfigurationUseCase,
    private val observeThemes: ObserveThemesUseCase,
    private val snackbarService: SnackbarService
) : Presenter<AppState, AppEvent> {
    @Composable
    override fun present(): Store<AppState, AppEvent> {
        val config by observeGuiConfiguration().collectProgressiveAsState()
        val themes by observeThemes().collectProgressiveAsState()

        val cfg = config.valueOrNull
        val themeCatalog = themes.valueOrNull
        val theme = cfg?.let { c ->
            themeCatalog?.byId?.get(c.selectedThemeId)
                ?: themeCatalog?.byId?.get(Configuration.DEFAULT_THEME_ID)
        }
        val accent = theme?.accents?.find { it.id == cfg.selectedAccent }?.color?.value

        return AppState(
            config = cfg,
            theme = theme,
            accentColor = accent,
            isMinimized = screen.isMinimized,
            onWindowEvent = screen.onWindowEvent
        ) + {
            when (it) {
                AppEvent.ToggleDrawer -> backgroundScope.launch {
                    updateGuiConfiguration { copy(menuDrawerOpen = !menuDrawerOpen) }
                }
            }
        }
    }

    @CircuitInject(AppScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: AppScreen): AppPresenter
    }
}
