package app.vercors.feature.project.domain.model

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.project.domain.model.modpack.ModpackFileKind

fun ProjectType.toContentKindOrNull(): ContentKind? = when (this) {
    ProjectType.Mod -> ContentKind.Mod
    ProjectType.ResourcePack -> ContentKind.ResourcePack
    ProjectType.ShaderPack -> ContentKind.ShaderPack
    ProjectType.Modpack, ProjectType.DataPack, ProjectType.World -> null
}

fun ContentKind.toProjectType(): ProjectType = when (this) {
    ContentKind.Mod -> ProjectType.Mod
    ContentKind.ResourcePack -> ProjectType.ResourcePack
    ContentKind.ShaderPack -> ProjectType.ShaderPack
}

fun ModpackFileKind.toContentKindOrNull(): ContentKind? = when (this) {
    ModpackFileKind.Mod -> ContentKind.Mod
    ModpackFileKind.ResourcePack -> ContentKind.ResourcePack
    ModpackFileKind.ShaderPack -> ContentKind.ShaderPack
    ModpackFileKind.Other -> null
}

fun ModpackFileKind.toProjectType(): ProjectType = when (this) {
    ModpackFileKind.Mod, ModpackFileKind.Other -> ProjectType.Mod
    ModpackFileKind.ResourcePack -> ProjectType.ResourcePack
    ModpackFileKind.ShaderPack -> ProjectType.ShaderPack
}

val ContentKind.folderName: String
    get() = when (this) {
        ContentKind.Mod -> "mods"
        ContentKind.ResourcePack -> "resourcepacks"
        ContentKind.ShaderPack -> "shaderpacks"
    }
