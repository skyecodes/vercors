package app.vercors.launcher.common.data.instance

import app.vercors.launcher.common.domain.instance.model.InstanceModpackInfo
import app.vercors.shared.domain.project.model.PlatformId
import kotlinx.serialization.Serializable

@Serializable
data class InstanceModpackInfoData(
    val platformId: PlatformId,
    val projectId: String,
    val fileId: String,
)

fun InstanceModpackInfoData.toDomain(): InstanceModpackInfo = InstanceModpackInfo(
    platformId = platformId,
    projectId = projectId,
    fileId = fileId,
)

fun InstanceModpackInfo.toData(): InstanceModpackInfoData = InstanceModpackInfoData(
    platformId = platformId,
    projectId = projectId,
    fileId = fileId,
)
