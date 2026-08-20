package app.vercors.feature.project.data.modpack

import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.modpack.ModpackFileKind
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import arrow.core.raise.Raise
import kotlinx.io.files.Path

/** Parses a downloaded modpack package file (already on disk at [zipPath]) into a platform-agnostic [ModpackManifest]. */
interface ModpackManifestParser {
    context(_: Raise<ModpackResolutionError>)
    suspend fun parse(zipPath: Path, projectId: ProjectId, fileId: String): ModpackManifest
}

/** Derives a [ModpackFileKind] from a manifest file's target path (e.g. `mods/foo.jar` -> [ModpackFileKind.Mod]). */
internal fun String.toModpackFileKind(): ModpackFileKind = when (substringBefore('/')) {
    "mods" -> ModpackFileKind.Mod
    "resourcepacks" -> ModpackFileKind.ResourcePack
    "shaderpacks" -> ModpackFileKind.ShaderPack
    else -> ModpackFileKind.Other
}
