package app.vercors.launcher.gui.home

import app.vercors.launcher.gui.instance.create.CreateInstanceRoute
import app.vercors.launcher.gui.instance.details.InstanceDetailsRoute
import app.vercors.launcher.gui.navigation.appNavigation
import app.vercors.launcher.gui.project.details.ProjectDetailsRoute
import app.vercors.shared.ui.core.container
import org.koin.core.module.dsl.new
import org.koin.dsl.module

val HomeModule = module {
    container { new(::HomeContainer) }
    appNavigation<HomeRoute> { _, navigator ->
        HomeScreen(
            container = container(),
            navigateToCreateInstance = { navigator.navigateTo(CreateInstanceRoute) },
            navigateToInstanceDetails = { navigator.navigateTo(InstanceDetailsRoute(it)) },
            navigateToProjectDetails = { navigator.navigateTo(ProjectDetailsRoute(it)) }
        )
    }
}
