package app.vercors.launcher.common.domain.instance.model

import app.vercors.shared.domain.project.model.PlatformId

data class InstanceModpackInfo(
    val platformId: PlatformId,
    val projectId: String,
    val fileId: String,
)
