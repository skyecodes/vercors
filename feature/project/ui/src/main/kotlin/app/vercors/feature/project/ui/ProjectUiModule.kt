package app.vercors.feature.project.ui

import app.vercors.core.ui.component.AppText
import app.vercors.feature.project.ui.details.ProjectDetailsRoute
import app.vercors.feature.project.ui.list.ProjectListContainer
import app.vercors.feature.project.ui.list.ProjectListRoute
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.shared.ui.appNavigation
import app.vercors.shared.ui.container
import org.koin.core.module.dsl.new
import org.koin.dsl.module

val ProjectUiModule = module {
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
