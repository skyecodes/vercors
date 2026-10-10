package app.vercors.launcher.gui

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import app.vercors.core.filesystem.defaultAppRoot
import app.vercors.core.filesystem.ensureRootReachable
import app.vercors.core.filesystem.readAppRoot
import app.vercors.core.filesystem.writeAppRoot
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.shared.ui.*
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.util.Logger
import com.hashsequence.coilresvg.ResvgDecoder
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import dev.nucleusframework.application.*
import dev.nucleusframework.composenativetray.tray.api.Tray
import dev.nucleusframework.core.runtime.NucleusApp
import dev.nucleusframework.notification.common.NotificationManager
import dev.zacsweers.metro.createGraphFactory
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.io.files.Path
import okio.Path.Companion.toPath
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

private class Slf4jCoilLogger(override var minLevel: Logger.Level = Logger.Level.Warn) : Logger {
    private val logger = KotlinLogging.logger {}

    override fun log(tag: String, level: Logger.Level, message: String?, throwable: Throwable?) {
        val log = { "$tag: $message" }
        when (level) {
            Logger.Level.Verbose -> logger.trace(throwable, log)
            Logger.Level.Debug -> logger.debug(throwable, log)
            Logger.Level.Info -> logger.info(throwable, log)
            Logger.Level.Warn -> logger.warn(throwable, log)
            Logger.Level.Error -> logger.error(throwable, log)
        }
    }
}

internal data class RootResolution(val root: Path, val unreachable: Boolean)

private fun resolveRoot(configuredRoot: Path, defaultRoot: Path): RootResolution =
    if (ensureRootReachable(configuredRoot)) RootResolution(configuredRoot, unreachable = false)
    else RootResolution(defaultRoot, unreachable = true)

fun main(args: Array<String>) {
    val defaultRoot = defaultAppRoot(NucleusApp.appId)
    val initialResolution = readAppRoot(defaultRoot)?.let { resolveRoot(it, defaultRoot) }

    nucleusApplication(args) {
        aotTraining(AOT_TRAINING_DURATION)
        // Training runs unattended on a fresh profile, so it can't wait on the first-launch setup window
        val trainingResolution = RootResolution(defaultRoot, unreachable = false).takeIf { isAotTraining }
        var resolution by remember { mutableStateOf(initialResolution ?: trainingResolution) }
        val current = resolution
        if (current == null) {
            FirstLaunchSetupWindow(
                defaultRoot = defaultRoot,
                onConfirm = { chosen ->
                    writeAppRoot(defaultRoot, chosen)
                    resolution = RootResolution(chosen, unreachable = false)
                },
            )
        } else {
            val appGraph = remember(current.root) { initializeApp(current.root) }
            MainApp(appGraph, rootUnreachable = current.unreachable)
        }
    }
}

private fun initializeApp(root: Path): AppGraph {
    lateinit var logger: KLogger
    lateinit var appGraph: AppGraph
    lateinit var scope: CoroutineScope
    scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, e ->
            logger.error(e) { "Uncaught exception in background scope" }
            scope.launch { appGraph.snackbarService.error { "Uncaught exception in background scope" } }
        }
    )
    appGraph = createGraphFactory<AppGraph.Factory>().create(scope, root)
    System.setProperty("app.vercors.logDir", appGraph.appPaths.logs.toString())
    logger = KotlinLogging.logger {}
    logger.info { "Hello World!" }
    NotificationManager.initialize()

    appGraph.metaApiConnectivity // forces eager construction so its poll loop starts now

    Runtime.getRuntime().addShutdownHook(
        Thread {
            scope.cancel()
            logger.info { "Goodbye World!" }
        }
    )
    return appGraph
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun NucleusApplicationScope.MainApp(appGraph: AppGraph, rootUnreachable: Boolean) {
    LaunchedEffect(rootUnreachable) {
        if (rootUnreachable) {
            appGraph.snackbarService.warn { stringResource(Res.string.setupRootUnreachableWarning) }
        }
    }

    val windowState = rememberWindowState(size = DpSize(1280.dp, 720.dp))

    setSingletonImageLoaderFactory {
        ImageLoader.Builder(it)
            .logger(Slf4jCoilLogger())
            .components {
                add(KtorNetworkFetcherFactory(appGraph.httpClient))
                add(ResvgDecoder.Factory())
            }
            .diskCache(
                DiskCache.Builder()
                    .directory(appGraph.appPaths.imageCache.toString().toPath())
                    .build()
            )
            .build()
    }

    val instances by appGraph.observeInstanceList().collectProgressiveAsState()
    val hasRunningInstance by derivedStateOf {
        instances.valueOrNull?.any { it.getOrNull()?.status is InstanceStatus.Running } == true
    }

    var trayHidden by remember { mutableStateOf(false) }
    LaunchedEffect(hasRunningInstance) {
        if (!hasRunningInstance) trayHidden = false
    }
    SingleInstanceRestoreEffect { trayHidden = false }

    var showQuitConfirmation by remember { mutableStateOf(false) }

    val configuration by appGraph.observeConfiguration().collectProgressiveAsState()
    val themes by appGraph.observeThemes().collectProgressiveAsState()
    val logoTint = Color(
        configuration.valueOrNull?.let { config ->
            themes.valueOrNull?.selectedTheme(config)?.selectedAccentColor(config)
        } ?: BRAND_ACCENT_COLOR
    )

    Tray(
        icon = vectorResource(SharedRes.drawable.logo),
        tint = logoTint,
        tooltip = appGraph.appName,
        primaryAction = { trayHidden = false },
    ) {
        Item(stringResource(SharedRes.string.open)) { trayHidden = false }
        Divider()
        Item(stringResource(SharedRes.string.quit)) {
            if (hasRunningInstance) {
                trayHidden = false
                showQuitConfirmation = true
            } else {
                exitApplication()
            }
        }
    }

    DecoratedWindow(
        onCloseRequest = { if (hasRunningInstance) trayHidden = true else exitApplication() },
        undecorated = true,
        visible = !trayHidden,
        state = windowState,
        title = appGraph.appName,
        icon = rememberLogoPainter(logoTint),
        minimumSize = DpSize(960.dp, 540.dp),
    ) {
        CompositionLocalProvider(
            LocalQuitConfirmation provides QuitConfirmationState(
                requested = showQuitConfirmation,
                onDismiss = { showQuitConfirmation = false },
            ),
            LocalAppGraph provides appGraph,
        ) {
            CircuitCompositionLocals(appGraph.circuit) {
                CircuitContent(AppScreen)
            }
        }
    }
}
