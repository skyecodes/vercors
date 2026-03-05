package app.vercors.feature.project.backend.modrinth

import app.vercors.feature.project.api.dto.*
import app.vercors.feature.project.backend.iconMap
import app.vercors.feature.project.backend.modrinth.dto.*
import arrow.core.raise.Raise
import arrow.core.raise.context.raise

context(_: Raise<UnsupportedProjectTypeData>)
internal fun buildFacets(
    projectType: ProjectTypeData? = null,
    loaders: List<ProjectLoaderData>? = null,
    gameVersions: List<String>? = null,
    categories: List<String>? = null
): String? {
    val parts = mutableListOf<String>()
    projectType?.let { parts.add("[\"project_type:${projectType.toModrinthString()}\"]") }
    loaders?.let { parts.add(it.joinToString(",", "[", "]") { loader -> "\"categories:$loader\"" }) }
    gameVersions?.let { parts.add(it.joinToString(",", "[", "]") { gameVersion -> "\"versions:$gameVersion\"" }) }
    categories?.let { parts.add(it.joinToString(",", "[", "]") { category -> "\"categories:$category\"" }) }
    return if (parts.isEmpty()) null else parts.joinToString(",", "[", "]")
}

context(_: Raise<UnsupportedProjectTypeData>)
private fun ProjectTypeData.toModrinthString(): String = when (this) {
    ProjectTypeData.Mod -> "mod"
    ProjectTypeData.Modpack -> "modpack"
    ProjectTypeData.ResourcePack -> "resourcepack"
    ProjectTypeData.ShaderPack -> "shader"
    ProjectTypeData.DataPack -> "datapack"
    ProjectTypeData.World -> raise(UnsupportedProjectTypeData(this))
}

internal fun ModrinthProjectSearchResult.convert(): ProjectSearchResponseData = ProjectSearchResponseData(
    results = hits.map { it.convert() },
    total = totalHits
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

internal fun ModrinthProjectType.convert(): ProjectTypeData? = when (value) {
    ModrinthProjectType.MOD -> ProjectTypeData.Mod
    ModrinthProjectType.MODPACK -> ProjectTypeData.Modpack
    ModrinthProjectType.RESOURCEPACK -> ProjectTypeData.ResourcePack
    ModrinthProjectType.SHADERPACK -> ProjectTypeData.ShaderPack
    ModrinthProjectType.DATAPACK -> ProjectTypeData.DataPack
    else -> null
}

internal fun ModrinthCategory.convert(): ProjectCategoryData = ProjectCategoryData(
    id = name,
    slug = name,
    name = null,
    icon = iconMap[name]?.let { IconNameData(it) } ?: icon.ifBlank { null }?.let { IconSvgData(it) }
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

internal fun ProjectSortByData.toModrinthIndex(): String = when (this) {
    ProjectSortByData.Relevance -> "relevance"
    ProjectSortByData.Downloads -> "downloads"
    ProjectSortByData.DateUpdated -> "updated"
    ProjectSortByData.DateCreated -> "newest"
}
