package app.vercors.feature.instance.domain.model

data class ManualDownloadItem(
    val fileName: String,
    val downloadUrl: String,
    val downloaded: Boolean
)
