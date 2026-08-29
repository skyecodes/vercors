package app.vercors.feature.config.ui

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import app.vercors.core.system.OsType
import app.vercors.core.system.SystemService
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
import app.vercors.shared.domain.model.NativeLibraryOptions
import app.vercors.shared.domain.model.NativeLibraryType
import app.vercors.shared.domain.usecase.DetectNativeLibraryPathUseCase
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
import kotlinx.coroutines.CoroutineScope
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
    private val detectNativeLibraryPath: DetectNativeLibraryPathUseCase,
    private val systemService: SystemService,
) : Presenter<ConfigurationState, ConfigurationEvent> {
    @Composable
    override fun present(): Store<ConfigurationState, ConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val themeList by observeThemeViewList()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val javaInstallations by observeJavaInstallations().collectProgressiveAsState()
        val (javaAddError, setJavaAddError) = remember { mutableStateOf<String?>(null) }
        val (javaScanInProgress, setJavaScanInProgress) = remember { mutableStateOf(false) }
        val totalMemoryMb = remember {
            ((getTotalPhysicalMemory() ?: (16384L * 1024 * 1024)) / (1024 * 1024)).toInt()
        }
        val showNativeLibraryOptions = systemService.osType == OsType.Linux
        var detectedGlfwPath by remember { mutableStateOf<String?>(null) }
        var detectedOpenAlPath by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(showNativeLibraryOptions) {
            if (showNativeLibraryOptions) {
                detectedGlfwPath = detectNativeLibraryPath(NativeLibraryType.Glfw)
                detectedOpenAlPath = detectNativeLibraryPath(NativeLibraryType.OpenAl)
            }
        }
        val cfg = configuration.valueOrNull
        val selectedTheme = themeList.find { it.id == cfg?.selectedThemeId }
        val selectedAccent = selectedTheme?.accents?.find { it.id == cfg?.selectedAccent }
        val jvmMemoryOptions = cfg?.jvmMemoryOptions ?: JvmMemoryOptions()
        val nativeLibraryOptions = cfg?.nativeLibraryOptions ?: NativeLibraryOptions()

        return ConfigurationState(
            selectedTheme = selectedTheme,
            themeList = themeList,
            selectedAccent = selectedAccent,
            animations = cfg?.animations,
            uiScalePercent = cfg?.uiScalePercent,
            javaInstallations = javaInstallations.valueOrNull ?: emptyList(),
            preferredJavaByMajorVersion = cfg?.preferredJavaByMajorVersion ?: emptyMap(),
            javaAddError = javaAddError,
            javaScanInProgress = javaScanInProgress,
            jvmMemoryOptions = jvmMemoryOptions,
            totalMemoryMb = totalMemoryMb,
            nativeLibraryOptions = nativeLibraryOptions,
            showNativeLibraryOptions = showNativeLibraryOptions,
            detectedGlfwPath = detectedGlfwPath,
            detectedOpenAlPath = detectedOpenAlPath,
        ) + { event ->
            handleEvent(
                event = event,
                presenterScope = presenterScope,
                themeList = themeList,
                setJavaAddError = setJavaAddError,
                javaScanInProgress = javaScanInProgress,
                setJavaScanInProgress = setJavaScanInProgress
            )
        }
    }

    private fun handleEvent(
        event: ConfigurationEvent,
        presenterScope: CoroutineScope,
        themeList: List<ThemeView>,
        setJavaAddError: (String?) -> Unit,
        javaScanInProgress: Boolean,
        setJavaScanInProgress: (Boolean) -> Unit,
    ) {
        when (event) {
            is ConfigurationEvent.SelectTheme -> presenterScope.launch {
                selectTheme(themeList.first { t -> t.id == event.themeId })
            }

            is ConfigurationEvent.SelectAccent -> presenterScope.launch {
                updateConfiguration { copy(selectedAccent = event.accentId) }
            }

            ConfigurationEvent.ToggleAnimations -> presenterScope.launch {
                updateConfiguration { copy(animations = !animations) }
            }

            is ConfigurationEvent.SetUiScalePercent -> presenterScope.launch {
                updateConfiguration { copy(uiScalePercent = event.value) }
            }

            is ConfigurationEvent.AddJavaInstallation ->
                handleAddJavaInstallation(event.path, presenterScope, setJavaAddError)

            ConfigurationEvent.RescanJavaInstallations ->
                handleRescanJavaInstallations(presenterScope, javaScanInProgress, setJavaScanInProgress)

            is ConfigurationEvent.SetPreferredJava -> presenterScope.launch {
                updateConfiguration {
                    copy(
                        preferredJavaByMajorVersion = preferredJavaByMajorVersion +
                                (event.majorVersion to event.installationId)
                    )
                }
            }

            ConfigurationEvent.DismissJavaAddError -> setJavaAddError(null)

            is ConfigurationEvent.SetMinMemoryMb -> presenterScope.launch {
                updateConfiguration { copy(jvmMemoryOptions = jvmMemoryOptions.copy(minMemoryMb = event.value)) }
            }

            is ConfigurationEvent.SetMaxMemoryMb -> handleSetMaxMemoryMb(event.value, presenterScope)

            is ConfigurationEvent.SetExtraJvmArgs -> presenterScope.launch {
                updateConfiguration { copy(jvmMemoryOptions = jvmMemoryOptions.copy(extraArgs = event.value)) }
            }

            is ConfigurationEvent.ToggleUseSystemGlfw -> presenterScope.launch {
                updateConfiguration {
                    copy(nativeLibraryOptions = nativeLibraryOptions.copy(useSystemGlfw = event.checked))
                }
            }

            is ConfigurationEvent.SetGlfwPath -> presenterScope.launch {
                updateConfiguration { copy(nativeLibraryOptions = nativeLibraryOptions.copy(glfwPath = event.value)) }
            }

            is ConfigurationEvent.ToggleUseSystemOpenAl -> presenterScope.launch {
                updateConfiguration {
                    copy(nativeLibraryOptions = nativeLibraryOptions.copy(useSystemOpenAl = event.checked))
                }
            }

            is ConfigurationEvent.SetOpenAlPath -> presenterScope.launch {
                updateConfiguration { copy(nativeLibraryOptions = nativeLibraryOptions.copy(openAlPath = event.value)) }
            }
        }
    }

    private fun handleAddJavaInstallation(
        path: String,
        presenterScope: CoroutineScope,
        setJavaAddError: (String?) -> Unit,
    ) {
        presenterScope.launch {
            either { addJavaInstallation(path) }.onLeft { error ->
                setJavaAddError(
                    when (error) {
                        is JavaInstallationError.NotAJavaInstallation -> error.path
                    }
                )
            }
        }
    }

    private fun handleRescanJavaInstallations(
        presenterScope: CoroutineScope,
        javaScanInProgress: Boolean,
        setJavaScanInProgress: (Boolean) -> Unit,
    ) {
        if (javaScanInProgress) return
        setJavaScanInProgress(true)
        presenterScope.launch {
            try {
                scanJavaInstallations()
                snackbarService.success { stringResource(ConfigRes.string.javaScanCompleted) }
            } finally {
                setJavaScanInProgress(false)
            }
        }
    }

    private fun handleSetMaxMemoryMb(value: Int?, presenterScope: CoroutineScope) {
        presenterScope.launch {
            updateConfiguration {
                val newMax = value
                val clampedMin =
                    jvmMemoryOptions.minMemoryMb?.let { min -> newMax?.let { max -> minOf(min, max) } ?: min }
                copy(jvmMemoryOptions = jvmMemoryOptions.copy(maxMemoryMb = newMax, minMemoryMb = clampedMin))
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
