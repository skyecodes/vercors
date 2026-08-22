package app.vercors.feature.config.ui

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.feature.game.domain.model.JavaInstallationError
import app.vercors.feature.game.domain.usecase.AddJavaInstallationUseCase
import app.vercors.feature.game.domain.usecase.ObserveJavaInstallationsUseCase
import app.vercors.feature.game.domain.usecase.ScanJavaInstallationsUseCase
import app.vercors.feature.theme.domain.model.ThemeDefinition
import app.vercors.feature.theme.domain.model.ThemeNamedColor
import app.vercors.feature.theme.domain.usecase.ObserveThemesUseCase
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.usecase.GetTotalPhysicalMemoryUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.success
import arrow.core.raise.context.either
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.collectAsRetainedState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.value
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

@CircuitInject(ConfigurationScreen::class, AppScope::class)
@Inject
class ConfigurationPresenter(
    private val observeThemes: ObserveThemesUseCase,
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
    private val observeJavaInstallations: ObserveJavaInstallationsUseCase,
    private val scanJavaInstallations: ScanJavaInstallationsUseCase,
    private val addJavaInstallation: AddJavaInstallationUseCase,
    private val snackbarService: SnackbarService,
    private val getTotalPhysicalMemory: GetTotalPhysicalMemoryUseCase,
) : Presenter<ConfigurationState, ConfigurationEvent> {
    @Composable
    override fun present(): Store<ConfigurationState, ConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val themeList by observeThemeViewList()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val javaInstallations by observeJavaInstallations().collectProgressiveAsState()
        var javaAddError by remember { mutableStateOf<String?>(null) }
        var javaScanInProgress by remember { mutableStateOf(false) }
        val totalMemoryMb = remember {
            ((getTotalPhysicalMemory() ?: (16384L * 1024 * 1024)) / (1024 * 1024)).toInt()
        }
        val cfg = configuration.valueOrNull
        val selectedTheme = themeList.find { it.id == cfg?.selectedThemeId }
        val selectedAccent = selectedTheme?.accents?.find { it.id == cfg?.selectedAccent }
        val jvmMemoryOptions = cfg?.jvmMemoryOptions ?: JvmMemoryOptions()

        return ConfigurationState(
            selectedTheme = selectedTheme,
            themeList = themeList,
            selectedAccent = selectedAccent,
            animations = cfg?.animations,
            javaInstallations = javaInstallations.valueOrNull ?: emptyList(),
            preferredJavaByMajorVersion = cfg?.preferredJavaByMajorVersion ?: emptyMap(),
            javaAddError = javaAddError,
            javaScanInProgress = javaScanInProgress,
            jvmMemoryOptions = jvmMemoryOptions,
            totalMemoryMb = totalMemoryMb,
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

                is ConfigurationEvent.AddJavaInstallation -> presenterScope.launch {
                    either { addJavaInstallation(it.path) }.onLeft { error ->
                        javaAddError = when (error) {
                            is JavaInstallationError.NotAJavaInstallation -> error.path
                        }
                    }
                }

                ConfigurationEvent.RescanJavaInstallations -> if (!javaScanInProgress) {
                    javaScanInProgress = true
                    presenterScope.launch {
                        try {
                            scanJavaInstallations()
                            snackbarService.success { stringResource(ConfigRes.string.javaScanCompleted) }
                        } finally {
                            javaScanInProgress = false
                        }
                    }
                }

                is ConfigurationEvent.SetPreferredJava -> presenterScope.launch {
                    updateConfiguration {
                        copy(preferredJavaByMajorVersion = preferredJavaByMajorVersion + (it.majorVersion to it.installationId))
                    }
                }

                ConfigurationEvent.DismissJavaAddError -> javaAddError = null

                is ConfigurationEvent.SetMinMemoryMb -> presenterScope.launch {
                    updateConfiguration { copy(jvmMemoryOptions = jvmMemoryOptions.copy(minMemoryMb = it.value)) }
                }

                is ConfigurationEvent.SetMaxMemoryMb -> presenterScope.launch {
                    updateConfiguration {
                        val newMax = it.value
                        val clampedMin =
                            jvmMemoryOptions.minMemoryMb?.let { min -> newMax?.let { max -> minOf(min, max) } ?: min }
                        copy(jvmMemoryOptions = jvmMemoryOptions.copy(maxMemoryMb = newMax, minMemoryMb = clampedMin))
                    }
                }

                is ConfigurationEvent.SetExtraJvmArgs -> presenterScope.launch {
                    updateConfiguration { copy(jvmMemoryOptions = jvmMemoryOptions.copy(extraArgs = it.value)) }
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
