package app.vercors.feature.instance.domain.model.export

data class InstanceExportFile(
    val relativePath: String,
    val path: String,
    val size: Long,
    val sha1: String?,
    val curseForgeFingerprint: Long?
)
