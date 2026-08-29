package app.vercors.feature.project.data.modpack

import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.model.modpack.ModpackFileKind
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import arrow.core.raise.Raise
import kotlinx.io.files.Path
import opensavvy.progress.Progress

/** Parses a downloaded modpack package file (already on disk at [zipPath]) into a platform-agnostic [ModpackManifest]. */
interface ModpackManifestParser {
    /**
     * Parses the pack's own manifest/index — name, game version, loader, overrides dirs. Local-file-only and fast:
     * [ModpackManifest.files] may be left unresolved (e.g. CurseForge only lists project/file IDs here) — call
     * [resolveFiles] to fill it in.
     */
    context(_: Raise<ModpackResolutionError>)
    suspend fun parse(zipPath: Path, projectId: ProjectId, fileId: String): ModpackManifest

    /**
     * Resolves [ModpackManifest.files] for a manifest returned by [parse]. May perform per-mod network lookups
     * (CurseForge, reporting [onProgress] as it goes) or be a no-op (Modrinth, already resolved during [parse]).
     */
    context(_: Raise<ModpackResolutionError>)
    suspend fun resolveFiles(
        zipPath: Path,
        manifest: ModpackManifest,
        onProgress: (Progress) -> Unit = {}
    ): ModpackManifest
}

/** Derives a [ModpackFileKind] from a manifest file's target path (e.g. `mods/foo.jar` -> [ModpackFileKind.Mod]). */
internal fun String.toModpackFileKind(): ModpackFileKind = when (substringBefore('/')) {
    "mods" -> ModpackFileKind.Mod
    "resourcepacks" -> ModpackFileKind.ResourcePack
    "shaderpacks" -> ModpackFileKind.ShaderPack
    else -> ModpackFileKind.Other
}

/** Derives a [ModpackFileKind] from a resolved [ProjectType] — used when a manifest gives no per-file path
 *  (e.g. CurseForge) and the destination folder has to be inferred from the file's own project type instead. */
internal fun ProjectType.toModpackFileKind(): ModpackFileKind = when (this) {
    ProjectType.Mod, ProjectType.Modpack -> ModpackFileKind.Mod
    ProjectType.ResourcePack -> ModpackFileKind.ResourcePack
    ProjectType.ShaderPack -> ModpackFileKind.ShaderPack
    ProjectType.DataPack, ProjectType.World -> ModpackFileKind.Other
}

/** Inverse of [String.toModpackFileKind] — the folder a resolved file of this [ModpackFileKind] belongs in. */
internal fun ModpackFileKind.toFolderName(): String = when (this) {
    ModpackFileKind.Mod -> "mods"
    ModpackFileKind.ResourcePack -> "resourcepacks"
    ModpackFileKind.ShaderPack -> "shaderpacks"
    ModpackFileKind.Other -> "mods"
}
