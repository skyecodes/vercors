package app.vercors.launcher.common.data.instance

import app.vercors.launcher.common.domain.instance.Instance
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant

@Serializable
data class InstanceData(
    val name: String,
    val gameVersion: String,
    val modLoader: InstanceModLoaderData?,
    val modpackInfo: InstanceModpackInfoData?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastPlayedAt: Instant?,
    val playTime: Duration,
) {
}

fun InstanceData.toDomain(id: String): Instance = Instance(
    name = name,
    id = id,
    gameVersion = gameVersion,
    modLoader = modLoader?.toDomain(),
    modpackInfo = modpackInfo?.toDomain(),
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
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastPlayedAt = lastPlayedAt,
    playTime = playTime,
)