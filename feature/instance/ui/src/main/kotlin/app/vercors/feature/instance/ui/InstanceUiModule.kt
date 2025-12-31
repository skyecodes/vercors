package app.vercors.feature.instance.ui

import app.vercors.feature.instance.ui.create.CreateInstanceContainer
import app.vercors.feature.instance.ui.create.CreateInstanceRoute
import app.vercors.feature.instance.ui.create.CreateInstanceScreen
import app.vercors.feature.instance.ui.details.InstanceDetailsContainer
import app.vercors.feature.instance.ui.details.InstanceDetailsRoute
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.list.InstanceListContainer
import app.vercors.feature.instance.ui.list.InstanceListScreen
import app.vercors.shared.ui.appNavigation
import app.vercors.shared.ui.container
import org.koin.core.module.dsl.new
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val InstanceUiModule = module {
    container { new(::InstanceListContainer) }
    appNavigation<app.vercors.feature.instance.ui.list.InstanceListRoute> { _, navigator ->
        InstanceListScreen(
            container = container(),
            navigateToCreateInstance = { navigator.navigateTo(CreateInstanceRoute) },
            navigateToInstanceDetails = {
                navigator.navigateTo(
                    InstanceDetailsRoute(
                        it
                    )
                )
            }
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
            navigateToModpackList = { /*navigator.navigateTo(ProjectListRoute)*/ }, // TODO add navigation
            navigateToInstanceDetails = {
                navigator.navigateTo(InstanceDetailsRoute(it))
            }
        )
    }
}
