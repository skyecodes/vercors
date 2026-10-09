package app.vercors.feature.project.ui.list

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.shared.ui.Screen

data class ProjectListScreen(
    val initialType: ProjectType? = null,
    val initialGameVersion: String? = null,
    val initialLoader: ModLoaderType? = null,
    val sourceInstanceId: String? = null,
) : Screen
