package app.vercors.launcher.gui

import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.error
import app.vercors.shared.ui.open
import app.vercors.shared.ui.quit
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.util.Logger
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MountainSnow
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.SingleInstanceRestoreEffect
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.composenativetray.tray.api.Tray
import dev.nucleusframework.notification.common.NotificationManager
import dev.zacsweers.metro.createGraphFactory
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import okio.Path.Companion.toPath
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

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

@OptIn(ExperimentalCoilApi::class)
fun main(args: Array<String>) {
    lateinit var logger: KLogger
    lateinit var appGraph: AppGraph
    lateinit var scope: CoroutineScope
    scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, e ->
            logger.error(e) { "Uncaught exception in background scope" }
            scope.launch { appGraph.snackbarService.error { "Uncaught exception in background scope" } }
        }
    )
    appGraph = createGraphFactory<AppGraph.Factory>().create(scope)
    System.setProperty("app.vercors.logDir", appGraph.appPaths.logs.toString())
    logger = KotlinLogging.logger {}
    logger.info { "Hello World!" }
    NotificationManager.initialize()

    appGraph.metaApiConnectivityPoller // force eager construction so its background poll loop starts now

    Runtime.getRuntime().addShutdownHook(
        Thread {
            scope.cancel()
            logger.info { "Goodbye World!" }
        }
    )

    nucleusApplication(args) {
        val windowState = rememberWindowState()
        windowState.size = DpSize(1280.dp, 720.dp)

        setSingletonImageLoaderFactory {
            ImageLoader.Builder(it)
                .logger(Slf4jCoilLogger())
                .components {
                    KtorNetworkFetcherFactory(appGraph.httpClient)
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

        Tray(
            icon = Lucide.MountainSnow,
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
}
