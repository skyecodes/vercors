package app.vercors.launcher.gui.project.list

import app.vercors.launcher.gui.navigation.Route
import app.vercors.launcher.gui.navigation.TopLevelDestination
import app.vercors.shared.domain.project.model.ProjectType
import kotlinx.serialization.Serializable

@Serializable
data class ProjectListRoute(
    val projectType: ProjectType = ProjectType.Modpack
) : Route {
    override val topLevelDestination get() = TopLevelDestination.Content
}
