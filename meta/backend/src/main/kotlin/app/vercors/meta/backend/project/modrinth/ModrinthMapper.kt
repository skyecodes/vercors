package app.vercors.meta.backend.project.modrinth

import app.vercors.meta.backend.project.modrinth.dto.ModrinthProjectResult
import app.vercors.meta.backend.project.modrinth.dto.ModrinthProjectSearchResult
import app.vercors.meta.lib.project.ProjectData
import app.vercors.meta.lib.project.ProjectSearchResponseData
import app.vercors.meta.lib.project.ProjectTypeData

internal fun buildFacets(
    projectType: ProjectTypeData? = null,
    categories: String? = null,
    versions: String? = null
): String {
    val parts = mutableListOf<String>()
    projectType?.let { parts.add("[\"project_type:${it.asModrinthString()}\"]") }
    categories?.let { parts.add("[\"categories:$it\"]") }
    versions?.let { parts.add("[\"versions:$it\"]") }
    return parts.joinToString(
        separator = ",",
        prefix = "[",
        postfix = "]"
    )
}

private fun ProjectTypeData.asModrinthString(): String = when (this) {
    ProjectTypeData.Mod -> "mod"
    ProjectTypeData.Modpack -> "modpack"
    ProjectTypeData.ResourcePack -> "resourcepack"
    ProjectTypeData.ShaderPack -> "shader"
}

internal fun ModrinthProjectSearchResult.convert(): ProjectSearchResponseData = ProjectSearchResponseData(
    results = hits.map { it.convert() }
)

internal fun ModrinthProjectResult.convert(): ProjectData = ProjectData(
    id = projectId,
    name = title,
    description = description,
    author = author,
    url = "https://modrinth.com/project/$projectId",
    logoUrl = iconUrl,
    imageUrl = featuredGallery
)