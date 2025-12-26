package app.vercors.launcher.gui.project

import app.vercors.launcher.gui.navigation.appNavigation
import app.vercors.launcher.gui.project.details.ProjectDetailsRoute
import app.vercors.launcher.gui.project.list.ProjectListContainer
import app.vercors.launcher.gui.project.list.ProjectListRoute
import app.vercors.launcher.gui.project.list.ProjectListScreen
import app.vercors.shared.ui.core.component.AppText
import app.vercors.shared.ui.core.container
import org.koin.core.module.dsl.new
import org.koin.dsl.module

val ProjectModule = module {
    container { new(::ProjectListContainer) }
    appNavigation<ProjectListRoute> { _, navigator ->
        ProjectListScreen(
            container = container(),
            navigateToProjectDetails = { navigator.navigateTo(ProjectDetailsRoute(it)) }
        )
    }
    appNavigation<ProjectDetailsRoute> { _, _ ->
        AppText("Project details")
    }
}
