package app.vercors.feature.project.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class CurseForgeManifest(
    val minecraft: CurseForgeManifestMinecraft,
    val name: String,
    val files: List<CurseForgeManifestFile> = emptyList(),
    val overrides: String = "overrides"
)

@Serializable
internal data class CurseForgeManifestMinecraft(
    val version: String,
    val modLoaders: List<CurseForgeManifestModLoader> = emptyList()
)

@Serializable
internal data class CurseForgeManifestModLoader(val id: String, val primary: Boolean = false)

@Serializable
internal data class CurseForgeManifestFile(val projectID: Long, val fileID: Long, val required: Boolean = true)
