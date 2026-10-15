package app.vercors.feature.project.domain.model.update

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.project.domain.model.ProjectFile
import app.vercors.feature.project.domain.model.ProjectId

data class ContentUpdate(
    val kind: ContentKind,
    val fileName: String,
    val enabled: Boolean,
    val projectId: ProjectId,
    val current: ProjectFile,
    val latest: ProjectFile
)
