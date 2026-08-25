package app.vercors.feature.instance.data.dto

import app.vercors.feature.instance.domain.model.PendingManualDownload
import app.vercors.feature.instance.domain.usecase.ContentKind
import kotlinx.serialization.Serializable

@Serializable
enum class ContentKindData {
    Mod, ResourcePack, ShaderPack
}

fun ContentKindData.toDomain(): ContentKind = when (this) {
    ContentKindData.Mod -> ContentKind.Mod
    ContentKindData.ResourcePack -> ContentKind.ResourcePack
    ContentKindData.ShaderPack -> ContentKind.ShaderPack
}

fun ContentKind.toData(): ContentKindData = when (this) {
    ContentKind.Mod -> ContentKindData.Mod
    ContentKind.ResourcePack -> ContentKindData.ResourcePack
    ContentKind.ShaderPack -> ContentKindData.ShaderPack
}

@Serializable
data class PendingManualDownloadData(
    val fileName: String,
    val downloadUrl: String,
    val size: Long?,
    val sha1: String?,
    val md5: String?,
    val contentKind: ContentKindData,
    val platformId: String,
    val providerProjectId: String,
    val providerFileId: String,
)

fun PendingManualDownloadData.toDomain(): PendingManualDownload = PendingManualDownload(
    fileName = fileName,
    downloadUrl = downloadUrl,
    size = size,
    sha1 = sha1,
    md5 = md5,
    contentKind = contentKind.toDomain(),
    platformId = platformId,
    providerProjectId = providerProjectId,
    providerFileId = providerFileId,
)

fun PendingManualDownload.toData(): PendingManualDownloadData = PendingManualDownloadData(
    fileName = fileName,
    downloadUrl = downloadUrl,
    size = size,
    sha1 = sha1,
    md5 = md5,
    contentKind = contentKind.toData(),
    platformId = platformId,
    providerProjectId = providerProjectId,
    providerFileId = providerFileId,
)
