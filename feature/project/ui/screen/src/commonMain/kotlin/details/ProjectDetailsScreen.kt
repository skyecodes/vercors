package app.vercors.feature.project.ui.details

import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.shared.ui.Screen

data class ProjectDetailsScreen(
    val projectId: ProjectId,
    /** The instance the user was on before navigating here, if any — carried through so Install can pre-select it. */
    val preferredInstanceId: String? = null,
) : Screen
