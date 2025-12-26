package app.vercors.launcher.gui.instance

import app.vercors.launcher.gui.instance.create.CreateInstanceContainer
import app.vercors.launcher.gui.instance.create.CreateInstanceRoute
import app.vercors.launcher.gui.instance.create.CreateInstanceScreen
import app.vercors.launcher.gui.instance.details.InstanceDetailsContainer
import app.vercors.launcher.gui.instance.details.InstanceDetailsRoute
import app.vercors.launcher.gui.instance.details.InstanceDetailsScreen
import app.vercors.launcher.gui.instance.list.InstanceListContainer
import app.vercors.launcher.gui.instance.list.InstanceListRoute
import app.vercors.launcher.gui.instance.list.InstanceListScreen
import app.vercors.launcher.gui.navigation.appNavigation
import app.vercors.launcher.gui.project.list.ProjectListRoute
import app.vercors.shared.ui.core.container
import org.koin.core.module.dsl.new
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val InstanceModule = module {
    container { new(::InstanceListContainer) }
    appNavigation<InstanceListRoute> { _, navigator ->
        InstanceListScreen(
            container = container(),
            navigateToCreateInstance = { navigator.navigateTo(CreateInstanceRoute) },
            navigateToInstanceDetails = { navigator.navigateTo(InstanceDetailsRoute(it)) }
        )
    }

    container { new(::InstanceDetailsContainer) }
    appNavigation<InstanceDetailsRoute> { route, _ ->
        InstanceDetailsScreen(
            container = container(
                key = route.toString(),
                params = { parametersOf(route.instanceId) }
            )
        )
    }

    container { new(::CreateInstanceContainer) }
    appNavigation<CreateInstanceRoute> { _, navigator ->
        CreateInstanceScreen(
            container = container(),
            navigateToModpackList = { navigator.navigateTo(ProjectListRoute) },
            navigateToInstanceDetails = { navigator.navigateTo(InstanceDetailsRoute(it)) }
        )
    }
}
