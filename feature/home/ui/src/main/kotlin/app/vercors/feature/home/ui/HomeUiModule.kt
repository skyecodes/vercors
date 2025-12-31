package app.vercors.feature.home.ui

import app.vercors.feature.instance.ui.create.CreateInstanceRoute
import app.vercors.feature.instance.ui.details.InstanceDetailsRoute
import app.vercors.feature.project.ui.details.ProjectDetailsRoute
import app.vercors.shared.ui.appNavigation
import app.vercors.shared.ui.container
import org.koin.core.module.dsl.new
import org.koin.dsl.module

val HomeUiModule = module {
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
