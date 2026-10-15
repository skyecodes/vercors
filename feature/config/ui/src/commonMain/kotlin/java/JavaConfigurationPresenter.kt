package app.vercors.feature.config.ui.java

import androidx.compose.runtime.*
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.model.withoutJavaInstallations
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.feature.config.ui.ConfigRes
import app.vercors.feature.config.ui.javaScanCompleted
import app.vercors.feature.java.domain.model.JavaInstallationError
import app.vercors.feature.java.domain.usecase.AddJavaInstallationUseCase
import app.vercors.feature.java.domain.usecase.ObserveJavaInstallationsUseCase
import app.vercors.feature.java.domain.usecase.ScanJavaInstallationsUseCase
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.usecase.GetTotalPhysicalMemoryMbUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.success
import arrow.core.raise.context.either
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

@Inject
class JavaConfigurationPresenter(
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
    private val observeJavaInstallations: ObserveJavaInstallationsUseCase,
    private val scanJavaInstallations: ScanJavaInstallationsUseCase,
    private val addJavaInstallation: AddJavaInstallationUseCase,
    private val getTotalPhysicalMemoryMb: GetTotalPhysicalMemoryMbUseCase,
    private val snackbarService: SnackbarService,
) : Presenter<JavaConfigurationState, JavaConfigurationEvent> {
    @Composable
    override fun present(): Store<JavaConfigurationState, JavaConfigurationEvent> {
        val presenterScope = rememberCoroutineScope()
        val configuration by observeConfiguration().collectProgressiveAsState()
        val javaInstallations by observeJavaInstallations().collectProgressiveAsState()
        val (javaAddError, setJavaAddError) = remember { mutableStateOf<String?>(null) }
        val (javaScanInProgress, setJavaScanInProgress) = remember { mutableStateOf(false) }
        val totalMemoryMb = remember { getTotalPhysicalMemoryMb() }
        val cfg = configuration.valueOrNull

        return JavaConfigurationState(
            javaInstallations = javaInstallations.valueOrNull ?: emptyList(),
            preferredJavaByMajorVersion = cfg?.preferredJavaByMajorVersion ?: emptyMap(),
            javaAddError = javaAddError,
            javaScanInProgress = javaScanInProgress,
            jvmMemoryOptions = cfg?.jvmMemoryOptions ?: JvmMemoryOptions(),
            totalMemoryMb = totalMemoryMb,
        ) + { event ->
            handleEvent(
                event = event,
                presenterScope = presenterScope,
                setJavaAddError = setJavaAddError,
                javaScanInProgress = javaScanInProgress,
                setJavaScanInProgress = setJavaScanInProgress,
            )
        }
    }

    private fun handleEvent(
        event: JavaConfigurationEvent,
        presenterScope: CoroutineScope,
        setJavaAddError: (String?) -> Unit,
        javaScanInProgress: Boolean,
        setJavaScanInProgress: (Boolean) -> Unit,
    ) {
        when (event) {
            is JavaConfigurationEvent.OnJavaInstallationSelected ->
                handleAddJavaInstallation(event.path, presenterScope, setJavaAddError)

            JavaConfigurationEvent.OnRescanJavaClick ->
                handleRescanJavaInstallations(presenterScope, javaScanInProgress, setJavaScanInProgress)

            is JavaConfigurationEvent.OnPreferredJavaChange -> presenterScope.launch {
                updateConfiguration {
                    copy(
                        preferredJavaByMajorVersion = preferredJavaByMajorVersion +
                                (event.majorVersion to event.installationId)
                    )
                }
            }

            is JavaConfigurationEvent.OnForgetInstallation -> handleForgetInstallation(
                event.installationId,
                presenterScope
            )

            JavaConfigurationEvent.OnJavaAddErrorDismiss -> setJavaAddError(null)

            is JavaConfigurationEvent.OnMinMemoryMbChange -> presenterScope.launch {
                updateConfiguration { copy(jvmMemoryOptions = jvmMemoryOptions.copy(minMemoryMb = event.value)) }
            }

            is JavaConfigurationEvent.OnMaxMemoryMbChange -> handleSetMaxMemoryMb(event.value, presenterScope)

            is JavaConfigurationEvent.OnExtraJvmArgsChange -> presenterScope.launch {
                updateConfiguration { copy(jvmMemoryOptions = jvmMemoryOptions.copy(extraArgs = event.value)) }
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

    private fun handleForgetInstallation(installationId: String, presenterScope: CoroutineScope) {
        presenterScope.launch {
            updateConfiguration { withoutJavaInstallations(setOf(installationId)) }
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
}
