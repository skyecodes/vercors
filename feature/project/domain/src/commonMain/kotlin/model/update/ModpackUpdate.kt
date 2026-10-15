package app.vercors.feature.project.domain.model.update

import app.vercors.feature.project.domain.model.ProjectFile
import app.vercors.feature.project.domain.model.ProjectId

data class ModpackUpdate(
    val projectId: ProjectId,
    val current: ProjectFile,
    val latest: ProjectFile
)
