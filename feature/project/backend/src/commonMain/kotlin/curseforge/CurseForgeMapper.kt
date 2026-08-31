package app.vercors.feature.project.backend.curseforge

import app.vercors.feature.project.api.dto.*
import app.vercors.feature.project.backend.curseforge.dto.*
import app.vercors.feature.project.backend.iconMap

internal fun CurseForgeProjectSearchResponse.convert(): ProjectSearchResponseData = ProjectSearchResponseData(
    results = data.map { it.convertBrief() },
    total = pagination.totalCount
)

internal fun CurseForgeProject.convertBrief(): BriefProjectData = BriefProjectData(
    id = id.toString(),
    platform = CURSEFORGE_PLATFORM_ID,
    name = name,
    type = toProjectType(),
    loaders = toProjectLoaders(),
    description = summary,
    author = authors.first().name,
    url = "https://www.curseforge.com/minecraft/${classId.toCurseForgeUrlSegment()}/$slug",
    downloads = downloadCount,
    lastUpdated = dateModified,
    logoUrl = logo.thumbnailUrl,
    imageUrl = screenshots.firstOrNull()?.url
)

internal fun CurseForgeProject.convert(descriptionHtml: String): ProjectData = ProjectData(
    id = id.toString(),
    platform = CURSEFORGE_PLATFORM_ID,
    name = name,
    type = toProjectType(),
    description = summary,
    url = "https://www.curseforge.com/minecraft/${classId.toCurseForgeUrlSegment()}/$slug",
    body = descriptionHtml.toMarkdown(),
    logoUrl = logo.thumbnailUrl,
    gameVersions = latestFilesIndexes.map { it.gameVersion }.distinct(),
    loaders = toProjectLoaders(),
    categories = categories.map { it.convert() },
    authors = authors.map { ProjectAuthorData(name = it.name, url = it.url) },
    links = ProjectLinksData(
        websiteUrl = links.websiteUrl,
        sourceUrl = links.sourceUrl,
        issuesUrl = links.issuesUrl,
        wikiUrl = links.wikiUrl,
        discordUrl = null
    )
)

// Falls back to Mod for a classId this launcher doesn't otherwise recognize — in practice unreachable, since
// project search already filters results down to known classIds.
private fun CurseForgeProject.toProjectType(): ProjectTypeData = classId.toProjectTypeData() ?: ProjectTypeData.Mod

private fun CurseForgeProject.toProjectLoaders(): List<ProjectLoaderData> =
    latestFilesIndexes.mapNotNull { it.modLoader?.toProjectLoaderDataOrNull() }.distinct()

private fun CurseForgeModLoaderType.toProjectLoaderDataOrNull(): ProjectLoaderData? = when (this) {
    CurseForgeModLoaderType.Forge -> ProjectLoaderData.Forge
    CurseForgeModLoaderType.NeoForge -> ProjectLoaderData.NeoForge
    CurseForgeModLoaderType.Fabric -> ProjectLoaderData.Fabric
    CurseForgeModLoaderType.Quilt -> ProjectLoaderData.Quilt
    CurseForgeModLoaderType.Any, CurseForgeModLoaderType.Cauldron, CurseForgeModLoaderType.LiteLoader -> null
}

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

internal fun Int?.toProjectTypeData(): ProjectTypeData? = when (this) {
    MOD_CLASS_ID -> ProjectTypeData.Mod
    MODPACK_CLASS_ID -> ProjectTypeData.Modpack
    RESOURCEPACK_CLASS_ID -> ProjectTypeData.ResourcePack
    SHADERPACK_CLASS_ID -> ProjectTypeData.ShaderPack
    DATAPACK_CLASS_ID -> ProjectTypeData.DataPack
    WORLD_CLASS_ID -> ProjectTypeData.World
    else -> null
}

// Falls back to "mc-mods" for a classId this launcher doesn't otherwise recognize — in practice unreachable,
// since project search already filters results down to known classIds.
internal fun Int?.toCurseForgeUrlSegment(): String =
    (this.toProjectTypeData() ?: ProjectTypeData.Mod).toCurseForgeUrlSegment()

internal fun ProjectTypeData.toCurseForgeUrlSegment(): String = when (this) {
    ProjectTypeData.Mod -> "mc-mods"
    ProjectTypeData.Modpack -> "modpacks"
    ProjectTypeData.ResourcePack -> "texture-packs"
    ProjectTypeData.ShaderPack -> "shaders"
    ProjectTypeData.DataPack -> "data-packs"
    ProjectTypeData.World -> "worlds"
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

/**
 * [CurseForgeFile] has no clean per-file loader field: [CurseForgeFile.gameVersions] mixes real version
 * strings and loader names (e.g. `["1.20.1", "Forge"]`) with no separator. When the caller already filtered
 * by [loaderFilter] server-side, just echo it back; otherwise fall back to scanning [CurseForgeFile.gameVersions]
 * against the known loader-name set.
 */
internal fun CurseForgeFile.convert(loaderFilter: ProjectLoaderData?): ProjectFileData = ProjectFileData(
    id = id.toString(),
    displayName = displayName,
    fileName = fileName,
    gameVersions = sortableGameVersions.map { it.gameVersion }.distinct(),
    loaders = loaderFilter?.let { listOf(it) } ?: gameVersions.mapNotNull { it.toProjectLoaderDataOrNull() }.distinct(),
    releaseChannel = releaseType.toProjectReleaseChannelData(),
    size = fileLength,
    sha1 = hashes.firstOrNull { it.algo == CurseForgeHashAlgo.Sha1 }?.value,
    md5 = hashes.firstOrNull { it.algo == CurseForgeHashAlgo.Md5 }?.value,
    publishedAt = fileDate,
    dependencies = dependencies.filter { it.relationType == CurseForgeFileRelationType.RequiredDependency }
        .map { ProjectFileDependencyData(it.modId.toString()) }
)

private fun String.toProjectLoaderDataOrNull(): ProjectLoaderData? = when (this) {
    "Forge" -> ProjectLoaderData.Forge
    "NeoForge" -> ProjectLoaderData.NeoForge
    "Fabric" -> ProjectLoaderData.Fabric
    "Quilt" -> ProjectLoaderData.Quilt
    else -> null
}

internal fun CurseForgeFileReleaseType.toProjectReleaseChannelData(): ProjectReleaseChannelData = when (this) {
    CurseForgeFileReleaseType.Release -> ProjectReleaseChannelData.Release
    CurseForgeFileReleaseType.Beta -> ProjectReleaseChannelData.Beta
    CurseForgeFileReleaseType.Alpha -> ProjectReleaseChannelData.Alpha
}
