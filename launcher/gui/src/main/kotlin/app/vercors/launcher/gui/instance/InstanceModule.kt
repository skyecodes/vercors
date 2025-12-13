package app.vercors.launcher.gui.instance

import app.vercors.launcher.gui.instance.create.CreateInstanceContainer
import app.vercors.launcher.gui.instance.create.CreateInstanceRoute
import app.vercors.launcher.gui.instance.create.CreateInstanceScreen
import app.vercors.launcher.gui.instance.details.InstanceDetailsRoute
import app.vercors.launcher.gui.instance.list.InstanceListContainer
import app.vercors.launcher.gui.instance.list.InstanceListRoute
import app.vercors.launcher.gui.instance.list.InstanceListScreen
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.container
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

val InstanceModule = module {
    container { new(::InstanceListContainer) }
    navigation<InstanceListRoute> {
        InstanceListScreen(
            container = container(),
            navigateToInstanceDetailsAction = { }
        )
    }
    navigation<InstanceDetailsRoute> {
        AppText("Instance details")
    }
    container { new(::CreateInstanceContainer) }
    navigation<CreateInstanceRoute> {
        CreateInstanceScreen(
            container = container()
        )
    }
}
