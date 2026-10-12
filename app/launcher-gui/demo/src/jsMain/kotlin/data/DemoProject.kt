package app.vercors.launcher.demo.data

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.ProjectType

data class DemoProject(
    val id: String,
    val slug: String,
    val title: String,
    val type: ProjectType,
    val loaders: List<ModLoaderType>,
    val description: String,
    val author: String,
    val downloads: Long,
    val dateModified: String,
    val icon: String?,
    val banner: String?,
)
