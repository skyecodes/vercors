package app.vercors.launcher.gui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.nucleusApplication
import dev.zacsweers.metro.createGraphFactory
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import okio.Path.Companion.toPath

@OptIn(ExperimentalCoilApi::class)
fun main(args: Array<String>) {
    lateinit var logger: KLogger
    val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, e ->
            logger.error(e) { "Uncaught exception in background scope" }
        }
    )
    val appGraph = createGraphFactory<AppGraph.Factory>().create(scope)
    System.setProperty("app.vercors.logDir", appGraph.appPaths.logs.toString())
    appGraph.metaApiConnectivityPoller // force eager construction so its background poll loop starts now
    logger = KotlinLogging.logger {}
    logger.info { "Hello World!" }

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

        DecoratedWindow(
            onCloseRequest = ::exitApplication,
            undecorated = true,
            state = windowState,
            title = "vercors",
            minimumSize = DpSize(960.dp, 540.dp),
        ) {
            CircuitCompositionLocals(appGraph.circuit) {
                CircuitContent(AppScreen)
            }
        }
    }
}
