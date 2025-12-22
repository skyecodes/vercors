package app.vercors.meta.backend.project.modrinth

import app.vercors.meta.backend.project.iconMap
import app.vercors.meta.backend.project.modrinth.dto.*
import app.vercors.meta.lib.project.*

internal fun buildFacets(
    projectType: ProjectTypeData? = null,
    loaders: List<ProjectLoaderData>? = null,
    gameVersions: List<String>? = null,
    categories: List<String>? = null
): String {
    val parts = mutableListOf<String>()
    projectType?.let { parts.add("[\"project_type:${projectType.asModrinthString()}\"]") }
    loaders?.let { parts.add(it.joinToString(",", "[", "]") { loader -> "\"categories:$loader\"" }) }
    gameVersions?.let { parts.add(it.joinToString(",", "[", "]") { gameVersion -> "\"versions:$gameVersion\"" }) }
    categories?.let { parts.add(it.joinToString(",", "[", "]") { category -> "\"categories:$category\"" }) }
    return parts.joinToString(",", "[", "]")
}

private fun ProjectTypeData.asModrinthString(): String = when (this) {
    ProjectTypeData.Mod -> "mod"
    ProjectTypeData.Modpack -> "modpack"
    ProjectTypeData.ResourcePack -> "resourcepack"
    ProjectTypeData.ShaderPack -> "shader"
    ProjectTypeData.DataPack -> "datapack"
    ProjectTypeData.World -> error("Worlds are not supported by Modrinth")
}

internal fun ModrinthProjectSearchResult.convert(): ProjectSearchResponseData = ProjectSearchResponseData(
    results = hits.map { it.convert() }
)

internal fun ModrinthProjectResult.convert(): BriefProjectData = BriefProjectData(
    id = projectId,
    platform = MODRINTH_PLATFORM_ID,
    name = title,
    description = description,
    author = author,
    url = "https://modrinth.com/project/$projectId",
    downloads = downloads,
    lastUpdated = dateModified,
    logoUrl = iconUrl,
    imageUrl = featuredGallery
)

internal fun ModrinthProjectType.convert(): ProjectTypeData? = when (this) {
    ModrinthProjectType.Mod -> ProjectTypeData.Mod
    ModrinthProjectType.Modpack -> ProjectTypeData.Modpack
    ModrinthProjectType.ResourcePack -> ProjectTypeData.ResourcePack
    ModrinthProjectType.ShaderPack -> ProjectTypeData.ShaderPack
    ModrinthProjectType.DataPack -> ProjectTypeData.DataPack
}

internal fun ModrinthCategory.convert(): ProjectCategoryData = ProjectCategoryData(
    id = name,
    slug = name,
    name = null,
    icon = iconMap[name]
)

internal fun String.toModrinthProjectType(): ProjectTypeData? = when (this) {
    "mod" -> ProjectTypeData.Mod
    "modpack" -> ProjectTypeData.Modpack
    "resourcepack" -> ProjectTypeData.ResourcePack
    "shader" -> ProjectTypeData.ShaderPack
    "datapack" -> ProjectTypeData.DataPack
    else -> null
}

internal fun ModrinthProject.convert(): ProjectData = ProjectData(
    id = id,
    platform = MODRINTH_PLATFORM_ID,
    name = title,
    description = description,
    body = body,
    url = "https://modrinth.com/project/$id",
    logoUrl = iconUrl
)
