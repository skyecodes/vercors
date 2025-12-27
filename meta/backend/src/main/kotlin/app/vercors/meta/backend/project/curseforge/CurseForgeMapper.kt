package app.vercors.meta.backend.project.curseforge

import app.vercors.meta.backend.project.curseforge.dto.*
import app.vercors.meta.backend.project.iconMap
import app.vercors.meta.lib.project.*

internal fun CurseForgeProjectSearchResponse.convert(): ProjectSearchResponseData = ProjectSearchResponseData(
    results = data.map { it.convertBrief() },
    total = pagination.totalCount
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

internal fun CurseForgeCategory.convert(): ProjectCategoryData = ProjectCategoryData(
    id = "$id",
    slug = slug,
    name = name,
    icon = iconMap[slug]?.let { IconNameData(it) }
)

internal fun ProjectTypeData.toCurseForgeClassId(): Int = when (this) {
    ProjectTypeData.Mod -> MOD_CLASS_ID
    ProjectTypeData.Modpack -> MODPACK_CLASS_ID
    ProjectTypeData.ResourcePack -> RESOURCEPACK_CLASS_ID
    ProjectTypeData.ShaderPack -> SHADERPACK_CLASS_ID
    ProjectTypeData.DataPack -> DATAPACK_CLASS_ID
    ProjectTypeData.World -> WORLD_CLASS_ID
}

internal fun CurseForgeCategory.convertToType(): ProjectTypeData? = when (id) {
    MOD_CLASS_ID -> ProjectTypeData.Mod
    MODPACK_CLASS_ID -> ProjectTypeData.Modpack
    RESOURCEPACK_CLASS_ID -> ProjectTypeData.ResourcePack
    SHADERPACK_CLASS_ID -> ProjectTypeData.ShaderPack
    DATAPACK_CLASS_ID -> ProjectTypeData.DataPack
    WORLD_CLASS_ID -> ProjectTypeData.World
    else -> null
}

internal fun ProjectLoaderData.toCurseForgeModLoaderType(): CurseForgeModLoaderType = when (this) {
    ProjectLoaderData.Forge -> CurseForgeModLoaderType.Forge
    ProjectLoaderData.Fabric -> CurseForgeModLoaderType.Fabric
    ProjectLoaderData.Quilt -> CurseForgeModLoaderType.Quilt
    ProjectLoaderData.NeoForge -> CurseForgeModLoaderType.NeoForge
}

internal fun ProjectSortByData.toCurseForgeSortField(): CurseForgeProjectSearchSortField = when (this) {
    ProjectSortByData.Relevance -> CurseForgeProjectSearchSortField.Popularity
    ProjectSortByData.Downloads -> CurseForgeProjectSearchSortField.TotalDownloads
    ProjectSortByData.DateUpdated -> CurseForgeProjectSearchSortField.LastUpdated
    ProjectSortByData.DateCreated -> CurseForgeProjectSearchSortField.ReleasedDate
}
