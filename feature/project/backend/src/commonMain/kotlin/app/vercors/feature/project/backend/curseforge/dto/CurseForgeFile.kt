package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurseForgeFile(
    val alternateFileId: Int? = null,
    val dependencies: List<FileDependency>,
    val displayName: String,
    val downloadCount: Long,
    val downloadUrl: String? = null,
    val earlyAccessEndDate: CurseForgeInstant? = null,
    val exposeAsAlternative: Boolean? = null,
    val fileDate: CurseForgeInstant,
    val fileFingerprint: Long,
    val fileLength: Long,
    val fileName: String,
    val fileSizeOnDisk: Long? = null,
    val fileStatus: CurseForgeFileStatus,
    val gameId: Int,
    val gameVersions: List<String>,
    val hashes: List<FileHash>,
    val id: Int,
    val isAvailable: Boolean,
    val isEarlyAccessContent: Boolean? = null,
    val isServerPack: Boolean? = null,
    val modId: Int,
    val modules: List<FileModule>,
    val parentProjectFileId: Int? = null,
    val releaseType: CurseForgeFileReleaseType,
    val serverPackFileId: Int? = null,
    val sortableGameVersions: List<SortableGameVersion>
) {
    @Serializable
    data class FileDependency(
        val modId: Int,
        val relationType: CurseForgeFileRelationType
    )

    @Serializable
    data class FileHash(
        val algo: CurseForgeHashAlgo,
        val value: String
    )

    @Serializable
    data class FileModule(
        val fingerprint: Long,
        val name: String
    )

    @Serializable
    data class SortableGameVersion(
        val gameVersion: String,
        val gameVersionName: String,
        val gameVersionPadded: String,
        val gameVersionReleaseDate: CurseForgeInstant,
        val gameVersionTypeId: Int? = null
    )
}
