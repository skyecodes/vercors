package app.vercors.feature.project.data

import app.vercors.core.network.RemoteError
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.api.dto.*
import app.vercors.feature.project.domain.model.*
import app.vercors.shared.api.dto.InvalidParameterData

internal fun ProjectTypeData.toDomain(): ProjectType = when (this) {
    ProjectTypeData.Mod -> ProjectType.Mod
    ProjectTypeData.Modpack -> ProjectType.Modpack
    ProjectTypeData.ResourcePack -> ProjectType.ResourcePack
    ProjectTypeData.ShaderPack -> ProjectType.ShaderPack
    ProjectTypeData.DataPack -> ProjectType.DataPack
    ProjectTypeData.World -> ProjectType.World
}

internal fun ProjectType.toData(): ProjectTypeData = when (this) {
    ProjectType.Mod -> ProjectTypeData.Mod
    ProjectType.Modpack -> ProjectTypeData.Modpack
    ProjectType.ResourcePack -> ProjectTypeData.ResourcePack
    ProjectType.ShaderPack -> ProjectTypeData.ShaderPack
    ProjectType.DataPack -> ProjectTypeData.DataPack
    ProjectType.World -> ProjectTypeData.World
}

internal fun ProjectSortBy.toData(): ProjectSortByData = when (this) {
    ProjectSortBy.Relevance -> ProjectSortByData.Relevance
    ProjectSortBy.Downloads -> ProjectSortByData.Downloads
    ProjectSortBy.DateUpdated -> ProjectSortByData.DateUpdated
    ProjectSortBy.DateCreated -> ProjectSortByData.DateCreated
}

internal fun BriefProjectData.toDomain(): BriefProject = BriefProject(
    id = id from platform,
    name = name,
    type = type.toDomain(),
    loaders = loaders.map { it.toDomain() },
    description = description,
    author = author,
    url = url,
    downloads = downloads,
    lastUpdated = lastUpdated,
    logoUrl = logoUrl,
    imageUrl = imageUrl,
)

internal fun ProjectData.toDomain(): Project = Project(
    id = id from platform,
    name = name,
    type = type.toDomain(),
    description = description,
    body = body,
    url = url,
    logoUrl = logoUrl,
    gameVersions = gameVersions,
    loaders = loaders.map { it.toDomain() },
    categories = categories.map { it.toDomain() },
    authors = authors.map { it.toDomain() },
    links = links.toDomain()
)

internal fun ProjectLoaderData.toDomain(): ModLoaderType = when (this) {
    ProjectLoaderData.Forge -> ModLoaderType.Forge
    ProjectLoaderData.NeoForge -> ModLoaderType.NeoForge
    ProjectLoaderData.Fabric -> ModLoaderType.Fabric
    ProjectLoaderData.Quilt -> ModLoaderType.Quilt
}

internal fun ProjectAuthorData.toDomain(): ProjectAuthor = ProjectAuthor(name, url, avatarUrl)

internal fun ProjectLinksData.toDomain(): ProjectLinks =
    ProjectLinks(websiteUrl, sourceUrl, issuesUrl, wikiUrl, discordUrl)

internal fun ProjectPlatformData.toDomain(): ProjectPlatform = ProjectPlatform(
    id = id,
    name = name,
    baseUrl = baseUrl,
    categories = categories.map { (type, groups) ->
        type.toDomain() to groups.mapValues { (_, list) -> list.map { it.toDomain() } }
    }.toMap()
)

internal fun ProjectCategoryData.toDomain(): ProjectCategory = ProjectCategory(
    id = id,
    name = name,
    slug = slug,
    icon = icon?.toDomain()
)

internal fun IconData.toDomain(): ProjectCategory.Icon = when (this) {
    is IconNameData -> ProjectCategory.Icon.Named(name)
    is IconSvgData -> ProjectCategory.Icon.Data(svgString)
}

internal fun PlatformErrorData.toDomain(): PlatformError = PlatformError(error, description)

internal fun RemoteError.toPlatformError(): PlatformError = when (this) {
    RemoteError.Network -> PlatformError("network", "Could not connect to the server")
    RemoteError.Timeout -> PlatformError("timeout", "The server took too long to respond")
    RemoteError.Serialization -> PlatformError("serialization", "Received an unexpected response from the server")
    is RemoteError.Http -> PlatformError("http_$status", body)
}

internal fun PlatformNotFoundData.toDomain(): PlatformNotFound = PlatformNotFound(notFoundPlatformId)

internal fun ProjectNotFoundData.toDomain(): ProjectNotFound = ProjectNotFound(notFoundProjectId from platformId)

internal fun ProjectFileData.toDomain(platform: PlatformId): ProjectFile = ProjectFile(
    id = id,
    displayName = displayName,
    fileName = fileName,
    gameVersions = gameVersions,
    loaders = loaders.map { it.toDomain() },
    releaseChannel = releaseChannel.toDomain(),
    size = size,
    sha1 = sha1,
    md5 = md5,
    sha512 = sha512,
    downloadUrl = downloadUrl,
    publishedAt = publishedAt,
    dependencies = dependencies.map { it.projectId from platform },
    manualDownloadUrl = manualDownloadUrl,
    type = type?.toDomain()
)

internal fun FileHashes.toData(): FileHashesData = FileHashesData(sha1 = sha1, fingerprint = curseForgeFingerprint)

internal fun ProjectFileMatchData.toDomain(platform: PlatformId): ProjectFileMatch = ProjectFileMatch(
    sha1 = sha1,
    projectId = projectId from platform,
    file = file.toDomain(platform)
)

internal fun ProjectReleaseChannelData.toDomain(): ProjectReleaseChannel = when (this) {
    ProjectReleaseChannelData.Release -> ProjectReleaseChannel.Release
    ProjectReleaseChannelData.Beta -> ProjectReleaseChannel.Beta
    ProjectReleaseChannelData.Alpha -> ProjectReleaseChannel.Alpha
}

internal fun InvalidParameterData.toDomain(): PlatformError =
    PlatformError("InvalidParameter", "Invalid value '$value' for parameter $name")
