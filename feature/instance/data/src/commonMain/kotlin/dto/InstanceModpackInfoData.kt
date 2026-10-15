package app.vercors.feature.instance.data.dto

import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import kotlinx.serialization.Serializable

@Serializable
data class InstanceModpackInfoData(
    val platformId: String,
    val projectId: String,
    val fileId: String,
    val versionName: String? = null,
)

fun InstanceModpackInfoData.toDomain(): InstanceModpackInfo = InstanceModpackInfo(
    platformId = platformId,
    projectId = projectId,
    fileId = fileId,
    versionName = versionName,
)

fun InstanceModpackInfo.toData(): InstanceModpackInfoData = InstanceModpackInfoData(
    platformId = platformId,
    projectId = projectId,
    fileId = fileId,
    versionName = versionName,
)
