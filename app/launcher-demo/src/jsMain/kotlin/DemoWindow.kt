package app.vercors.launcher.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import app.vercors.core.ui.CoreRes
import app.vercors.core.ui.Inter
import app.vercors.core.ui.InterTight
import app.vercors.core.ui.theme.AppTheme
import app.vercors.feature.home.ui.Home
import app.vercors.feature.home.ui.HomeScreen
import app.vercors.feature.instance.ui.details.InstanceDetails
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.project.ui.list.ProjectList
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.launcher.demo.data.MONIFACTORY_ID
import app.vercors.launcher.shell.AppFrame
import app.vercors.launcher.shell.AppMenu
import app.vercors.launcher.shell.AppTopBar
import app.vercors.shared.ui.Screen
import coil3.compose.setSingletonImageLoaderFactory
import com.slack.circuit.sharedelements.ProvideAnimatedTransitionScope
import com.slack.circuit.sharedelements.SharedElementTransitionScope.AnimatedScope
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getFontResourceBytes
import org.jetbrains.compose.resources.getSystemResourceEnvironment
import kotlin.time.Duration.Companion.milliseconds

private const val UI_SCALE = 1.25f
private const val APP_NAME = "Vercors"
private val READY_DELAY = 500.milliseconds

@OptIn(ExperimentalResourceApi::class)
@Composable
fun DemoWindow(screen: DemoScreen, onReady: () -> Unit) {
    setSingletonImageLoaderFactory(::demoImageLoader)
    val dark by rememberSiteDarkMode()

    LaunchedEffect(Unit) {
        val environment = getSystemResourceEnvironment()
        getFontResourceBytes(environment, CoreRes.font.Inter)
        getFontResourceBytes(environment, CoreRes.font.InterTight)
        // Leaves time for the loaded fonts and bundled icons to reach a frame, so the fade-in reveals a finished one
        delay(READY_DELAY)
        onReady()
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = Density(constraints.maxWidth / DEMO_WINDOW_WIDTH_PX.toFloat() * UI_SCALE, fontScale = 1f)
        CompositionLocalProvider(LocalDensity provides density) {
            // Rebuilt rather than animated: the demo is hidden while the site's theme transition runs
            key(dark) {
                (AppTheme(
                    themeColors = if (dark) MOCHA_COLORS else LATTE_COLORS,
                    accentColor = if (dark) MOCHA_MAUVE else LATTE_MAUVE,
                )) {
                    DemoFrame(screen)
                }
            }
        }
    }
}

@Composable
private fun DemoFrame(screen: DemoScreen) {
    AppFrame(
        topBar = {
            AppTopBar(
                canGoBack = screen != DemoScreen.Home,
                canGoNext = false,
                onNavigateBack = {},
                onNavigateNext = {},
                appName = APP_NAME,
                isMaximized = false,
                onMinimize = {},
                onToggleMaximize = {},
                onClose = {},
            )
        },
        menu = {
            AppMenu(
                currentBackStackEntry = screen.menuScreen,
                isDrawerOpen = true,
                updateAvailable = false,
                onToggleDrawerClick = {},
                onMenuButtonClick = {},
            )
        },
    ) {
        DemoNavigationScope {
            when (screen) {
                DemoScreen.Home -> Home(store = remember { homeDemoStore() })
                DemoScreen.Instance -> InstanceDetails(store = remember { instanceDetailsDemoStore() })
                DemoScreen.Content -> ProjectList(store = remember { projectListDemoStore() })
            }
        }
    }
}

// Screens resolve their shared-element scope from the navigation transition, which the demo has none of
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DemoNavigationScope(content: @Composable () -> Unit) {
    AnimatedVisibility(visible = true) {
        ProvideAnimatedTransitionScope(AnimatedScope.Navigation, this) {
            content()
        }
    }
}

private val DemoScreen.menuScreen: Screen
    get() = when (this) {
        DemoScreen.Home -> HomeScreen
        DemoScreen.Instance -> InstanceDetailsScreen(MONIFACTORY_ID)
        DemoScreen.Content -> ProjectListScreen()
    }
