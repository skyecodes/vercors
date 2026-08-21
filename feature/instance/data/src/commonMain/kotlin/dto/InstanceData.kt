package app.vercors.feature.instance.data.dto

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.shared.data.AppPaths
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant

@Serializable
data class InstanceData(
    val name: String,
    val gameVersion: String,
    val modLoader: InstanceModLoaderData?,
    val modpackInfo: InstanceModpackInfoData?,
    val iconExtension: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastPlayedAt: Instant?,
    val playTime: Duration,
)

fun InstanceData.toDomain(id: String, appPaths: AppPaths): Instance = Instance(
    name = name,
    id = id,
    gameVersion = gameVersion,
    modLoader = modLoader?.toDomain(),
    modpackInfo = modpackInfo?.toDomain(),
    iconFilePath = iconExtension?.let { appPaths.instanceIconFile(id, it).toString() },
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastPlayedAt = lastPlayedAt,
    playTime = playTime,
)

fun Instance.toData(): InstanceData = InstanceData(
    name = name,
    gameVersion = gameVersion,
    modLoader = modLoader?.toData(),
    modpackInfo = modpackInfo?.toData(),
    iconExtension = iconFilePath?.substringAfterLast('.', "")?.takeIf { it.isNotEmpty() },
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastPlayedAt = lastPlayedAt,
    playTime = playTime,
)
