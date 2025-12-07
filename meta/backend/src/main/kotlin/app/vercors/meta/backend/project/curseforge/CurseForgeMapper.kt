package app.vercors.meta.backend.project.curseforge

import app.vercors.meta.backend.project.curseforge.dto.CurseForgeProject
import app.vercors.meta.backend.project.curseforge.dto.CurseForgeProjectSearchResponse
import app.vercors.meta.lib.project.BriefProjectData
import app.vercors.meta.lib.project.ProjectData
import app.vercors.meta.lib.project.ProjectSearchResponseData
import app.vercors.meta.lib.project.ProjectTypeData

internal fun CurseForgeProjectSearchResponse.convert(): ProjectSearchResponseData = ProjectSearchResponseData(
    results = data.map { it.convertBrief() }
)

internal fun CurseForgeProject.convertBrief(): BriefProjectData = BriefProjectData(
    id = id.toString(),
    platform = CURSEFORGE_PLATFORM_ID,
    name = name,
    description = summary,
    author = authors.first().name,
    url = "https://www.curseforge.com/minecraft/mc-mods/$slug",
    downloads = downloadCount,
    lastUpdated = dateModified,
    logoUrl = logo.thumbnailUrl,
    imageUrl = screenshots.firstOrNull()?.url
)

internal fun CurseForgeProject.convert(): ProjectData = ProjectData(
    id = id.toString(),
    platform = CURSEFORGE_PLATFORM_ID,
    name = name,
    description = summary,
    url = "https://www.curseforge.com/minecraft/mc-mods/$slug",
    body = null, // TODO: call /description endpoint to get full body
    logoUrl = logo.thumbnailUrl
)

internal fun ProjectTypeData.toClassId(): Int = when (this) {
    ProjectTypeData.Mod -> MOD_CLASS_ID
    ProjectTypeData.Modpack -> MODPACK_CLASS_ID
    ProjectTypeData.ResourcePack -> RESOURCEPACK_CLASS_ID
    ProjectTypeData.ShaderPack -> SHADERPACK_CLASS_ID
}
