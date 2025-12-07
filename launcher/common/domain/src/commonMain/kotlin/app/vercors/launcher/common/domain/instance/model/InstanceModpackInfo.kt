package app.vercors.launcher.common.domain.instance.model

import app.vercors.shared.domain.project.model.ProjectPlatform

data class InstanceModpackInfo(
    val platform: ProjectPlatform,
    val projectId: String,
    val version: String,
    val fileId: String,
)
