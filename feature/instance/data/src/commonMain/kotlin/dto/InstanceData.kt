package app.vercors.feature.instance.data.dto

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.dto.JvmMemoryOptionsData
import app.vercors.shared.data.dto.NativeLibraryOptionsData
import app.vercors.shared.data.dto.toData
import app.vercors.shared.data.dto.toDomain
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant

@Serializable
data class InstanceData(
    val name: String,
    val gameVersion: String,
    val modLoader: InstanceModLoaderData?,
    val modpackInfo: InstanceModpackInfoData?,
    val pendingManualDownloads: List<PendingManualDownloadData> = emptyList(),
    val installStage: InstanceInstallStageData? = null,
    val iconExtension: String? = null,
    val javaInstallationId: String? = null,
    val jvmMemoryOptions: JvmMemoryOptionsData = JvmMemoryOptionsData(),
    val nativeLibraryOptions: NativeLibraryOptionsData = NativeLibraryOptionsData(),
    val checkForUpdates: Boolean? = null,
    // Instances saved before default options existed must not get them applied retroactively
    val defaultOptionsApplied: Boolean = true,
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
    pendingManualDownloads = pendingManualDownloads.map { it.toDomain() },
    installStage = installStage?.toDomain(),
    iconFilePath = iconExtension?.let { appPaths.instanceIconFile(id, it).toString() },
    javaInstallationId = javaInstallationId,
    jvmMemoryOptions = jvmMemoryOptions.toDomain(),
    nativeLibraryOptions = nativeLibraryOptions.toDomain(),
    checkForUpdates = checkForUpdates,
    defaultOptionsApplied = defaultOptionsApplied,
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
    pendingManualDownloads = pendingManualDownloads.map { it.toData() },
    installStage = installStage?.toData(),
    iconExtension = iconFilePath?.substringAfterLast('.', "")?.takeIf { it.isNotEmpty() },
    javaInstallationId = javaInstallationId,
    jvmMemoryOptions = jvmMemoryOptions.toData(),
    nativeLibraryOptions = nativeLibraryOptions.toData(),
    checkForUpdates = checkForUpdates,
    defaultOptionsApplied = defaultOptionsApplied,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastPlayedAt = lastPlayedAt,
    playTime = playTime,
)
