package app.vercors.feature.project.data.modpack

import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.model.modpack.ModpackFileKind
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import arrow.core.raise.Raise
import kotlinx.io.files.Path
import opensavvy.progress.Progress

interface ModpackManifestParser {
    context(_: Raise<ModpackResolutionError>)
    suspend fun parse(zipPath: Path, projectId: ProjectId, fileId: String): ModpackManifest

    context(_: Raise<ModpackResolutionError>)
    suspend fun resolveFiles(
        zipPath: Path,
        manifest: ModpackManifest,
        onProgress: (Progress) -> Unit = {}
    ): ModpackManifest
}

internal fun String.toModpackFileKind(): ModpackFileKind = when (substringBefore('/')) {
    "mods" -> ModpackFileKind.Mod
    "resourcepacks" -> ModpackFileKind.ResourcePack
    "shaderpacks" -> ModpackFileKind.ShaderPack
    else -> ModpackFileKind.Other
}

// CurseForge manifests give no per-file path, so the folder comes from the project type
internal fun ProjectType.toModpackFileKind(): ModpackFileKind = when (this) {
    ProjectType.Mod, ProjectType.Modpack -> ModpackFileKind.Mod
    ProjectType.ResourcePack -> ModpackFileKind.ResourcePack
    ProjectType.ShaderPack -> ModpackFileKind.ShaderPack
    ProjectType.DataPack, ProjectType.World -> ModpackFileKind.Other
}

internal fun ModpackFileKind.toFolderName(): String = when (this) {
    ModpackFileKind.Mod -> "mods"
    ModpackFileKind.ResourcePack -> "resourcepacks"
    ModpackFileKind.ShaderPack -> "shaderpacks"
    ModpackFileKind.Other -> "mods"
}
