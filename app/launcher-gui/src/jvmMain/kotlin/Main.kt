package app.vercors.launcher.gui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import dev.zacsweers.metro.createGraphFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import okio.Path.Companion.toPath
import java.awt.Dimension

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val appGraph = createGraphFactory<AppGraph.Factory>().create(scope)
    System.setProperty("app.vercors.logDir", appGraph.appPaths.logs.toString())
    val logger = KotlinLogging.logger {}
    logger.info { "Hello World!" }

    application(exitProcessOnExit = false) {
        val windowState = rememberWindowState()
        windowState.size = DpSize(1280.dp, 720.dp)

        setSingletonImageLoaderFactory {
            ImageLoader.Builder(it)
                .diskCache(
                    DiskCache.Builder()
                        .directory(appGraph.appPaths.imageCache.toString().toPath())
                        .build()
                )
                .build()
        }
        Window(
            state = windowState,
            onCloseRequest = ::exitApplication,
            decoration = WindowDecoration.Undecorated(),
            title = "vercors",
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = Dimension(960, 540)
            }

            CompositionLocalProvider(LocalAppWindowScope provides this) {
                CircuitCompositionLocals(appGraph.circuit) {
                    CircuitContent(
                        AppScreen(
                            isMinimized = windowState.placement != WindowPlacement.Maximized,
                            onWindowEvent = {
                                when (it) {
                                    AppWindowEvent.Close -> exitApplication()
                                    AppWindowEvent.Maximize -> windowState.placement =
                                        when (windowState.placement) {
                                            WindowPlacement.Floating -> WindowPlacement.Maximized
                                            else -> WindowPlacement.Floating
                                        }

                                    AppWindowEvent.Minimize -> windowState.isMinimized = true
                                }
                            }
                        )
                    )
                }
            }
        }
    }
    scope.cancel()
    logger.info { "Goodbye World!" }
}
