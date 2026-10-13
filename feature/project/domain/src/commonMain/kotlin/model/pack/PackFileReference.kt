package app.vercors.feature.project.domain.model.pack

data class PackFileReference(
    val relativePath: String,
    val platformId: String,
    val projectId: String,
    val fileId: String,
    val sha1: String,
    val sha512: String?,
    val size: Long,
    val downloadUrl: String?
)
