package app.vercors.launcher.gui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import app.vercors.shared.ui.error
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import dev.zacsweers.metro.createGraphFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.stringResource
import java.awt.Dimension

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val logger = KotlinLogging.logger {}
    val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, e ->
            logger.error(e) { "Uncaught exception in background scope" }
        }
    )
    val appGraph = createGraphFactory<AppGraph.Factory>().create(scope)
    System.setProperty("app.vercors.logDir", appGraph.appPaths.logs.toString())
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
        CompositionLocalProvider(
            LocalWindowExceptionHandlerFactory provides WindowExceptionHandlerFactory { _ ->
                WindowExceptionHandler { throwable ->
                    logger.error(throwable) { "Uncaught exception in GUI composition" }
                    scope.launch {
                        appGraph.snackbarService.error { stringResource(Res.string.unexpectedError) }
                    }
                }
            }
        ) {
            Window(
                state = windowState,
                onCloseRequest = ::exitApplication,
                decoration = WindowDecoration.Undecorated(),
                title = "vercors",
            ) {
                LaunchedEffect(Unit) {
                    window.minimumSize = Dimension(960, 540)
                }

                val windowScope = this
                val appWindow = remember(windowState) {
                    object : AppWindow, WindowScope by windowScope {
                        override val isMaximized: Boolean
                            get() = windowState.placement == WindowPlacement.Maximized

                        override fun onEvent(event: AppWindowEvent) {
                            when (event) {
                                AppWindowEvent.Close -> exitApplication()
                                AppWindowEvent.Maximize -> windowState.placement =
                                    when (windowState.placement) {
                                        WindowPlacement.Floating -> WindowPlacement.Maximized
                                        else -> WindowPlacement.Floating
                                    }

                                AppWindowEvent.Minimize -> windowState.isMinimized = true
                            }
                        }
                    }
                }

                CompositionLocalProvider(LocalAppWindow provides appWindow) {
                    CircuitCompositionLocals(appGraph.circuit) {
                        CircuitContent(AppScreen)
                    }
                }
            }
        }
    }
    scope.cancel()
    logger.info { "Goodbye World!" }
}
