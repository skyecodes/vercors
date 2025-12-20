package app.vercors.launcher.common.data.instance

import app.vercors.launcher.common.data.platform.PlatformTypeData
import app.vercors.launcher.common.data.platform.toData
import app.vercors.launcher.common.data.platform.toDomain
import app.vercors.launcher.common.domain.instance.model.InstanceModpackInfo
import kotlinx.serialization.Serializable

@Serializable
data class InstanceModpackInfoData(
    val platform: PlatformTypeData,
    val projectId: String,
    val fileId: String,
)

fun InstanceModpackInfoData.toDomain(): InstanceModpackInfo = InstanceModpackInfo(
    platform = platform.toDomain(),
    projectId = projectId,
    fileId = fileId,
)

fun InstanceModpackInfo.toData(): InstanceModpackInfoData = InstanceModpackInfoData(
    platform = platform.toData(),
    projectId = projectId,
    fileId = fileId,
)
