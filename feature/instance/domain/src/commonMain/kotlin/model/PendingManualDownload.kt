package app.vercors.feature.instance.domain.model

data class PendingManualDownload(
    val fileName: String,
    val downloadUrl: String,
    val size: Long?,
    val sha1: String?,
    val md5: String?,
    val contentKind: ContentKind,
    val platformId: String,
    val providerProjectId: String,
    val providerFileId: String,
)
