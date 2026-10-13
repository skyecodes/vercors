package app.vercors.feature.project.domain.model.modpack

import app.vercors.feature.game.domain.model.ModLoaderType

data class ModpackManifest(
    val platformId: String,
    val projectId: String,
    val fileId: String,
    val name: String,
    val gameVersion: String,
    val loaderType: ModLoaderType?,
    val loaderVersion: String?,
    val files: List<ModpackManifestFile>,
    val overridesDirs: List<String>
)

enum class ModpackFileKind {
    Mod, ResourcePack, ShaderPack, Other
}

data class ModpackManifestFile(
    val targetPath: String,
    val kind: ModpackFileKind,
    val downloadUrl: String?,
    val sha1: String?,
    val md5: String? = null,
    val size: Long?,
    val providerProjectId: String?,
    val providerFileId: String?,
    val manualDownloadUrl: String? = null,
    val platformId: String? = null
)

data class InstalledModpackFile(
    val fileName: String,
    val kind: ModpackFileKind,
    val providerProjectId: String?,
    val providerFileId: String?,
    val platformId: String? = null
)
