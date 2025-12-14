package app.vercors.launcher.gui.home

import androidx.navigation3.ui.LocalNavAnimatedContentScope
import app.vercors.launcher.gui.instance.create.CreateInstanceRoute
import app.vercors.launcher.gui.instance.details.InstanceDetailsRoute
import app.vercors.launcher.gui.navigation.LocalNavigator
import app.vercors.launcher.gui.project.details.ProjectDetailsRoute
import app.vercors.shared.ui.LocalSharedTransitionScope
import app.vercors.shared.ui.container
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

val HomeModule = module {
    container { new(::HomeContainer) }
    navigation<HomeRoute> {
        with(LocalNavAnimatedContentScope.current) {
            with(LocalSharedTransitionScope.current) {
                val navigator = LocalNavigator.current
                HomeScreen(
                    container = container(),
                    navigateToCreateInstance = { navigator.navigateTo(CreateInstanceRoute) },
                    navigateToInstanceDetails = { navigator.navigateTo(InstanceDetailsRoute(it)) },
                    navigateToProjectDetails = { navigator.navigateTo(ProjectDetailsRoute(it)) }
                )
            }
        }
    }
}
