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
    loaders?.let {
        parts.add(
            it.joinToString(
                ",",
                "[",
                "]"
            ) { loader -> "\"categories:${loader.toModrinthString()}\"" })
    }
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

internal fun ProjectLoaderData.toModrinthString(): String = when (this) {
    ProjectLoaderData.Forge -> "forge"
    ProjectLoaderData.NeoForge -> "neoforge"
    ProjectLoaderData.Fabric -> "fabric"
    ProjectLoaderData.Quilt -> "quilt"
}

internal fun ModrinthProjectSearchResult.convert(): ProjectSearchResponseData = ProjectSearchResponseData(
    results = hits.map { it.convert() },
    total = totalHits
)

internal fun ModrinthProjectResult.convert(): BriefProjectData = BriefProjectData(
    id = projectId,
    platform = MODRINTH_PLATFORM_ID,
    name = title,
    // Falls back to Mod for a Modrinth type this launcher doesn't otherwise recognize — in practice unreachable,
    // since project search already filters results down to known types.
    type = projectType.convert() ?: ProjectTypeData.Mod,
    loaders = categories.orEmpty().mapNotNull { it.toProjectLoaderDataOrNull() },
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

internal fun ModrinthProject.convert(members: List<ModrinthProjectMember>): ProjectData = ProjectData(
    id = id,
    platform = MODRINTH_PLATFORM_ID,
    name = title,
    // Falls back to Mod for a Modrinth type this launcher doesn't otherwise recognize (e.g. "plugin") — in
    // practice unreachable, since project search already filters results down to known types.
    type = projectType.convert() ?: ProjectTypeData.Mod,
    description = description,
    body = body,
    url = "https://modrinth.com/project/$id",
    logoUrl = iconUrl,
    gameVersions = gameVersions,
    loaders = loaders.mapNotNull { it.toProjectLoaderDataOrNull() },
    categories = (categories + additionalCategories).map {
        ProjectCategoryData(id = it, slug = it, name = null, icon = iconMap[it]?.let(::IconNameData))
    },
    authors = members.filter { it.accepted }
        .sortedByDescending { it.user.role == ModrinthRole.Admin }
        .map {
            ProjectAuthorData(
                name = it.user.name ?: it.user.username,
                url = "https://modrinth.com/user/${it.user.username}",
                avatarUrl = it.user.avatarUrl
            )
        },
    links = ProjectLinksData(
        sourceUrl = sourceUrl,
        issuesUrl = issuesUrl,
        wikiUrl = wikiUrl,
        discordUrl = discordUrl
    )
)

private fun String.toProjectLoaderDataOrNull(): ProjectLoaderData? = when (this) {
    "forge" -> ProjectLoaderData.Forge
    "neoforge" -> ProjectLoaderData.NeoForge
    "fabric" -> ProjectLoaderData.Fabric
    "quilt" -> ProjectLoaderData.Quilt
    else -> null
}

/** Modrinth versions can carry multiple files (e.g. split/universal jar variants); keep the primary one. */
internal fun ModrinthVersion.convert(): ProjectFileData? {
    val file = files.firstOrNull { it.primary } ?: files.firstOrNull() ?: return null
    return ProjectFileData(
        id = id,
        displayName = name,
        fileName = file.filename,
        gameVersions = gameVersions,
        loaders = loaders.mapNotNull { it.toProjectLoaderDataOrNull() },
        releaseChannel = versionType.toProjectReleaseChannelData(),
        size = file.size,
        sha1 = file.hashes["sha1"],
        publishedAt = datePublished,
        dependencies = dependencies.filter { it.dependencyType.value == ModrinthDependencyType.REQUIRED }
            .mapNotNull { it.projectId?.let(::ProjectFileDependencyData) }
    )
}

private fun ModrinthVersionType.toProjectReleaseChannelData(): ProjectReleaseChannelData = when (value) {
    ModrinthVersionType.RELEASE -> ProjectReleaseChannelData.Release
    ModrinthVersionType.BETA -> ProjectReleaseChannelData.Beta
    ModrinthVersionType.ALPHA -> ProjectReleaseChannelData.Alpha
    else -> ProjectReleaseChannelData.Alpha
}

internal fun ProjectSortByData.toModrinthIndex(): String = when (this) {
    ProjectSortByData.Relevance -> "relevance"
    ProjectSortByData.Downloads -> "downloads"
    ProjectSortByData.DateUpdated -> "updated"
    ProjectSortByData.DateCreated -> "newest"
}
