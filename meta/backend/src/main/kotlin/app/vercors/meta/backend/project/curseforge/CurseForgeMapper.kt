package app.vercors.meta.backend.project.curseforge

import app.vercors.meta.backend.project.curseforge.dto.CurseForgeProject
import app.vercors.meta.backend.project.curseforge.dto.CurseForgeProjectSearchResponse
import app.vercors.meta.lib.project.ProjectData
import app.vercors.meta.lib.project.ProjectSearchResponseData
import app.vercors.meta.lib.project.ProjectTypeData

internal fun CurseForgeProjectSearchResponse.convert(): ProjectSearchResponseData = ProjectSearchResponseData(
    results = data.map { it.convert() }
)

internal fun CurseForgeProject.convert(): ProjectData = ProjectData(
    id = id.toString(),
    name = name,
    description = summary,
    author = authors.first().name,
    url = "https://www.curseforge.com/minecraft/mc-mods/$slug",
    logoUrl = logo.thumbnailUrl,
    imageUrl = screenshots.firstOrNull()?.url
)

internal fun ProjectTypeData.toClassId(): Int = when (this) {
    ProjectTypeData.Mod -> MOD_CLASS_ID
    ProjectTypeData.Modpack -> MODPACK_CLASS_ID
    ProjectTypeData.ResourcePack -> RESOURCEPACK_CLASS_ID
    ProjectTypeData.ShaderPack -> SHADERPACK_CLASS_ID
}