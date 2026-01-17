package app.vercors.launcher.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.vercors.core.domain.orNull
import app.vercors.core.ui.state.collectResourceAsStateWithLifecycle
import app.vercors.feature.config.gui.domain.usecase.ObserveGuiConfigurationUseCase
import app.vercors.feature.config.gui.domain.usecase.UpdateGuiConfigurationUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.plus
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

@AssistedInject
class AppPresenter(
    @Assisted private val screen: AppScreen,
    private val backgroundScope: CoroutineScope,
    private val observeGuiConfiguration: ObserveGuiConfigurationUseCase,
    private val updateGuiConfiguration: UpdateGuiConfigurationUseCase,
) : Presenter<AppState, AppEvent> {
    @Composable
    override fun present(): Store<AppState, AppEvent> {
        val guiConfig by observeGuiConfiguration().collectResourceAsStateWithLifecycle()

        return AppState(
            guiConfig = guiConfig.orNull(),
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
