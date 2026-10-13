package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurseForgeFingerprintsMatchesResult(
    val exactMatches: List<FingerprintMatch> = emptyList()
) {
    @Serializable
    data class FingerprintMatch(
        val id: Int,
        val file: CurseForgeFile
    )
}
