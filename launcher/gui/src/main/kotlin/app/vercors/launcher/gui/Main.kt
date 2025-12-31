package app.vercors.launcher.gui

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import app.vercors.feature.account.data.AccountDataModule
import app.vercors.feature.account.domain.AccountDomainModule
import app.vercors.feature.account.ui.AccountUiModule
import app.vercors.feature.config.data.ConfigDataModule
import app.vercors.feature.config.gui.data.GuiConfigDataModule
import app.vercors.feature.config.gui.domain.GuiConfigDomainModule
import app.vercors.feature.game.data.GameDataModule
import app.vercors.feature.game.domain.GameDomainModule
import app.vercors.feature.home.domain.HomeDomainModule
import app.vercors.feature.home.ui.HomeUiModule
import app.vercors.feature.instance.data.InstanceDataModule
import app.vercors.feature.instance.domain.InstanceDomainModule
import app.vercors.feature.instance.ui.InstanceUiModule
import app.vercors.feature.launcher.data.LauncherDataModule
import app.vercors.feature.launcher.domain.LauncherDomainModule
import app.vercors.feature.project.data.ProjectDataModule
import app.vercors.feature.project.domain.ProjectDomainModule
import app.vercors.feature.project.ui.ProjectUiModule
import app.vercors.launcher.gui.config.ConfigurationUiModule
import app.vercors.meta.client.MetaClientModule
import app.vercors.shared.data.SharedDataModule
import app.vercors.shared.data.commonDataProperties
import app.vercors.shared.domain.SharedDomainModule
import app.vercors.shared.ui.container
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
import org.koin.core.logger.PrintLogger
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import org.koin.ksp.generated.startKoin
import java.awt.Dimension

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val koinApplication = GUIApplication.startKoin {
        logger(PrintLogger()) // TODO why SLF4J not available??
        commonDataProperties()
        modules(AppModule, module { single { coroutineScope } })
    }
    initializeLauncher(koinApplication.koin)

    application(exitProcessOnExit = false) {
        val windowState = rememberWindowState()
        windowState.size = DpSize(1280.dp, 720.dp)

        KoinIsolatedContext(koinApplication) {
            val appPath = getKoin().get<Path>()

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

@KoinApplication(
    modules = [
        GUIModule::class, MetaClientModule::class,
        SharedDataModule::class, SharedDomainModule::class,
        AccountDataModule::class, AccountDomainModule::class,
        ConfigDataModule::class,
        GuiConfigDataModule::class, GuiConfigDomainModule::class,
        GameDataModule::class, GameDomainModule::class,
        HomeDomainModule::class,
        InstanceDataModule::class, InstanceDomainModule::class,
        LauncherDataModule::class, LauncherDomainModule::class,
        ProjectDataModule::class, ProjectDomainModule::class,
    ]
)
object GUIApplication

private val AppModule = module {
    includes(HomeUiModule, InstanceUiModule, ProjectUiModule, AccountUiModule, ConfigurationUiModule)
    container { new(::AppContainer) }
}

fun initializeLauncher(koin: Koin) {
    val appPath = koin.get<Path>()
    val logDir = Path(appPath, "logs")
    System.setProperty("app.vercors.logDir", logDir.toString())
}
