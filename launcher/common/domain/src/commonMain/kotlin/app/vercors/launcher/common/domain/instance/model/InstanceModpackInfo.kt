package app.vercors.launcher.common.domain.instance.model

import app.vercors.launcher.common.domain.platform.PlatformType

data class InstanceModpackInfo(
    val platform: PlatformType,
    val projectId: String,
    val version: String,
    val fileId: String,
)