package app.vercors.feature.project.domain.model.modpack

import app.vercors.feature.game.domain.model.ModLoaderType

/**
 * The resolved contents of a modpack's own package file (a Modrinth `.mrpack` or a CurseForge modpack zip),
 * platform-agnostic — see [app.vercors.feature.project.domain.repository.ProjectRepository.getModpackManifest].
 */
data class ModpackManifest(
    val platformId: String,
    val projectId: String,
    val fileId: String,
    val name: String,
    val gameVersion: String,
    val loaderType: ModLoaderType?,
    val loaderVersion: String?,
    val files: List<ModpackManifestFile>,
    /** Zip-relative folder(s) (e.g. `"overrides"`, `"client-overrides"`) whose contents get copied verbatim into the instance's game dir. */
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
    /** Set only when this file can't be downloaded automatically — the page a user can download it from manually. */
    val manualDownloadUrl: String? = null
)

data class InstalledModpackFile(
    val fileName: String,
    val kind: ModpackFileKind,
    val providerProjectId: String?,
    val providerFileId: String?
)
