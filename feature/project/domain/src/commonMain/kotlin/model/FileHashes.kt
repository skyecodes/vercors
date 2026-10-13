package app.vercors.feature.project.domain.model

data class FileHashes(
    val sha1: String,
    val curseForgeFingerprint: Long?
)
