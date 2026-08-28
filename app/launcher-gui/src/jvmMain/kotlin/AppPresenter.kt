package app.vercors.launcher.gui

import androidx.compose.runtime.*
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.ui.details.progress
import app.vercors.feature.theme.domain.usecase.ObserveThemesUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarMessage
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import opensavvy.state.progressive.valueOrNull

private val logger = KotlinLogging.logger {}
private const val SNACKBAR_DURATION_MS = 4000L

@CircuitInject(AppScreen::class, AppScope::class)
@Inject
class AppPresenter(
    private val backgroundScope: CoroutineScope,
    private val observeGuiConfiguration: ObserveConfigurationUseCase,
    private val updateGuiConfiguration: UpdateConfigurationUseCase,
    private val observeThemes: ObserveThemesUseCase,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val snackbarService: SnackbarService,
) : Presenter<AppState, AppEvent> {
    @Composable
    override fun present(): Store<AppState, AppEvent> {
        val config by observeGuiConfiguration().collectProgressiveAsState()
        val themes by observeThemes().collectProgressiveAsState()
        val instances by observeInstanceList().collectProgressiveAsState()

        var snackbarMessage by remember { mutableStateOf<SnackbarMessage?>(null) }
        val dismissRequests = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
        LaunchedEffect(Unit) {
            for (message in snackbarService.messages) {
                snackbarMessage = message
                withTimeoutOrNull(SNACKBAR_DURATION_MS) { dismissRequests.first() }
                snackbarMessage = null
            }
        }

        val cfg = config.valueOrNull
        val themeCatalog = themes.valueOrNull
        val theme = cfg?.let { c ->
            themeCatalog?.byId?.get(c.selectedThemeId)
                ?: themeCatalog?.byId?.get(Configuration.DEFAULT_THEME_ID)
        }
        val accent = theme?.accents?.find { it.id == cfg.selectedAccent }?.color?.value

        val installTaskbarProgress = instances.valueOrNull
            ?.let { list -> computeInstallTaskbarProgress(list.mapNotNull { it.getOrNull()?.status }) }
            ?: InstallTaskbarProgress.None

        return AppState(
            config = cfg,
            theme = theme,
            accentColor = accent,
            snackbarMessage = snackbarMessage,
            installTaskbarProgress = installTaskbarProgress,
        ) + {
            when (it) {
                AppEvent.ToggleDrawer -> backgroundScope.launch {
                    updateGuiConfiguration { copy(menuDrawerOpen = !menuDrawerOpen) }
                }

                AppEvent.DismissSnackbar -> dismissRequests.tryEmit(Unit)
            }
        }
    }
}

private fun computeInstallTaskbarProgress(statuses: List<InstanceStatus>): InstallTaskbarProgress {
    val active = statuses.filter { it is InstanceStatus.Installing || it is InstanceStatus.Launching }
    if (active.isEmpty()) return InstallTaskbarProgress.None
    val fractions = active.mapNotNull { it.progress }
    return if (fractions.isEmpty()) {
        InstallTaskbarProgress.Indeterminate
    } else {
        InstallTaskbarProgress.Fraction(fractions.average().toFloat())
    }
}
