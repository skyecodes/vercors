package app.vercors.launcher.gui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import app.vercors.launcher.common.data.CommonDataModule
import app.vercors.launcher.common.domain.CommonDomainModule
import app.vercors.launcher.gui.account.AccountListRoute
import app.vercors.launcher.gui.config.ConfigurationRoute
import app.vercors.launcher.gui.home.HomeRoute
import app.vercors.launcher.gui.instance.details.InstanceDetailsRoute
import app.vercors.launcher.gui.instance.list.InstanceListContainer
import app.vercors.launcher.gui.instance.list.InstanceListRoute
import app.vercors.launcher.gui.instance.list.InstanceListScreen
import app.vercors.launcher.gui.project.list.ProjectListRoute
import app.vercors.shared.ui.*
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.compose.KoinIsolatedContext
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.ksp.generated.startKoin
import java.awt.Dimension

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val koin = GUIApplication.startKoin { modules(AppModule, module { single { coroutineScope } }) }

    application(exitProcessOnExit = false) {
        val windowState = rememberWindowState()
        windowState.size = DpSize(1280.dp, 720.dp)

        KoinIsolatedContext(koin) {
            Window(
                state = windowState,
                onCloseRequest = ::exitApplication,
                decoration = WindowDecoration.Undecorated(),
                title = "vercors",
            ) {
                window.minimumSize = Dimension(960, 540)
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

@KoinApplication(modules = [CommonDomainModule::class, CommonDataModule::class])
object GUIApplication

@OptIn(KoinExperimentalAPI::class)
val AppModule = module {
    container { new(::AppContainer) }
    container { new(::InstanceListContainer) }
    navigation<HomeRoute> {
        Text("Home", style = Theme[textStyles][title])
    }
    navigation<InstanceListRoute> {
        InstanceListScreen(
            container = container(),
            navigateToInstanceDetailsAction = { }
        )
    }
    navigation<InstanceDetailsRoute> {
        Text("Instance details")
    }
    navigation<ProjectListRoute> {
        Text("Content", style = Theme[textStyles][subtitle])
    }
    navigation<ConfigurationRoute> {
        Text("Settings")
    }
    navigation<AccountListRoute> {
        Text("Account", style = Theme[textStyles][label])
    }
}