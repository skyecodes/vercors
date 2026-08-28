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
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MountainSnow
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import dev.nucleusframework.application.DecoratedWindow
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
    appGraph.metaApiConnectivityPoller // force eager construction so its background poll loop starts now
    logger = KotlinLogging.logger {}
    logger.info { "Hello World!" }
    NotificationManager.initialize()

    // nucleusApplication() hard-exits the process (exitProcess(0)) on the Tao backend the moment the
    // last window closes, so code placed after this call never runs. A shutdown hook still lets the
    // background scope wind down (and this log line print) before the JVM actually terminates.
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
        val hasRunningInstance = instances.valueOrNull
            ?.any { it.getOrNull()?.status is InstanceStatus.Running } == true

        var trayHidden by remember { mutableStateOf(false) }
        // Reopens the window on its own once the instance that caused the hide stops running.
        LaunchedEffect(hasRunningInstance) {
            if (!hasRunningInstance) trayHidden = false
        }

        Tray(
            icon = Lucide.MountainSnow,
            tooltip = stringResource(Res.string.appTitle),
            primaryAction = { trayHidden = false },
        ) {
            Item(stringResource(SharedRes.string.open)) { trayHidden = false }
            Divider()
            Item(stringResource(SharedRes.string.quit)) { exitApplication() }
        }

        DecoratedWindow(
            onCloseRequest = { if (hasRunningInstance) trayHidden = true else exitApplication() },
            undecorated = true,
            visible = !trayHidden,
            state = windowState,
            title = stringResource(Res.string.appTitle),
            minimumSize = DpSize(960.dp, 540.dp),
        ) {
            CircuitCompositionLocals(appGraph.circuit) {
                CircuitContent(AppScreen)
            }
        }
    }
}
