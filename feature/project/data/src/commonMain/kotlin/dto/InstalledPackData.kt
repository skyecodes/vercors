package app.vercors.feature.project.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class InstalledPackData(
    val name: String,
    val versionName: String? = null,
    val modpackInfo: VercorsPackModpackInfo? = null,
    val files: List<InstalledPackFileData> = emptyList(),
)

@Serializable
internal data class InstalledPackFileData(
    val path: String,
    val platformId: String? = null,
    val projectId: String? = null,
)
