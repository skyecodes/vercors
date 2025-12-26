package app.vercors.launcher.gui

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import app.vercors.launcher.common.data.AppPath
import app.vercors.launcher.common.data.CommonDataModule
import app.vercors.launcher.common.domain.CommonDomainModule
import app.vercors.launcher.gui.account.AccountModule
import app.vercors.launcher.gui.config.ConfigurationModule
import app.vercors.launcher.gui.home.HomeModule
import app.vercors.launcher.gui.instance.InstanceModule
import app.vercors.launcher.gui.project.ProjectModule
import app.vercors.shared.data.SharedDataModule
import app.vercors.shared.domain.SharedDomainModule
import app.vercors.shared.ui.core.container
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.io.files.Path
import okio.Path.Companion.toPath
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.getKoin
import org.koin.core.Koin
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.module.dsl.new
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module
import org.koin.ksp.generated.startKoin
import java.awt.Dimension

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val koinApplication = GUIApplication.startKoin { modules(AppModule, module { single { coroutineScope } }) }
    initializeLauncher(koinApplication.koin)

    application(exitProcessOnExit = false) {
        val windowState = rememberWindowState()
        windowState.size = DpSize(1280.dp, 720.dp)

        KoinIsolatedContext(koinApplication) {
            val appPath = getKoin().get<Path>(qualifier<AppPath>())

            setSingletonImageLoaderFactory {
                ImageLoader.Builder(it)
                    .diskCache(
                        DiskCache.Builder()
                            .directory(appPath.toString().toPath().resolve("cache").resolve("coil"))
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

                App(
                    container = container(),
                    onWindowEvent = {
                        when (it) {
                            AppWindowEvent.Close -> exitApplication()
                            AppWindowEvent.Maximize -> windowState.placement = when (windowState.placement) {
                                WindowPlacement.Floating -> WindowPlacement.Maximized
                                else -> WindowPlacement.Floating
                            }

                            AppWindowEvent.Minimize -> windowState.isMinimized = true
                        }
                    },
                    isMinimized = windowState.placement != WindowPlacement.Maximized
                )
            }
        }
    }
    coroutineScope.cancel()
}

@Module
@ComponentScan
class GUIModule

@KoinApplication(modules = [GUIModule::class, CommonDomainModule::class, CommonDataModule::class, SharedDomainModule::class, SharedDataModule::class])
object GUIApplication

private val AppModule = module {
    includes(HomeModule, InstanceModule, ProjectModule, AccountModule, ConfigurationModule)
    container { new(::AppContainer) }
}

fun initializeLauncher(koin: Koin) {
    val appPath = koin.get<Path>(qualifier<AppPath>())
    val logDir = Path(appPath, "logs")
    System.setProperty("app.vercors.logDir", logDir.toString())
}
