package app.vercors.feature.instance.domain.model

data class InstanceModpackInfo(
    val platformId: String,
    val projectId: String,
    val fileId: String,
    val versionName: String? = null,
)
