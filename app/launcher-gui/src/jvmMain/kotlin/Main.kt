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
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.animation.AnimatedNavDecorator
import com.slack.circuit.runtime.ExperimentalCircuitApi
import com.slack.circuit.runtime.navigation.NavArgument
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dev.zacsweers.metro.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.io.files.Path
import okio.Path.Companion.toPath
import java.awt.Dimension

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val appGraph = createGraphFactory<AppGraph.Factory>().create(scope)
    val logDir = Path(appGraph.appPath, "logs")
    System.setProperty("app.vercors.logDir", logDir.toString())
    val logger = KotlinLogging.logger {}
    logger.info { "Hello World!" }

    application(exitProcessOnExit = false) {
        val windowState = rememberWindowState()
        windowState.size = DpSize(1280.dp, 720.dp)

        setSingletonImageLoaderFactory {
            ImageLoader.Builder(it)
                .diskCache(
                    DiskCache.Builder()
                        .directory(
                            appGraph.appPath.toString().toPath().resolve("data").resolve("cache").resolve("image")
                        )
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

@DependencyGraph(AppScope::class)
internal interface AppGraph {
    val appPath: Path
    val circuit: Circuit

    @OptIn(ExperimentalCircuitApi::class)
    @Provides
    @SingleIn(AppScope::class)
    fun provideCircuit(
        presenterFactories: Set<Presenter.Factory>,
        uiFactories: Set<Ui.Factory>,
    ): Circuit {
        return Circuit.Builder()
            .addPresenterFactories(presenterFactories)
            .addUiFactories(uiFactories)
            .setAnimatedNavDecoratorFactory(object : AnimatedNavDecorator.Factory {
                override fun <T : NavArgument> create(): AnimatedNavDecorator<T, *> {
                    return AppDecorator()
                }
            })
            .build()
    }

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides coroutineScope: CoroutineScope): AppGraph
    }
}
