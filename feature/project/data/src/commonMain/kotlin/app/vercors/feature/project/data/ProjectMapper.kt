package app.vercors.feature.project.data

import app.vercors.feature.project.api.dto.*
import app.vercors.feature.project.domain.model.*

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
    description = description,
    body = body,
    url = url,
    logoUrl = logoUrl
)

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

internal fun UnsupportedProjectTypeData.toDomain(): UnsupportedProjectType =
    UnsupportedProjectType(projectType.toDomain())

internal fun PlatformErrorData.toDomain(): PlatformError = PlatformError(error, description)

internal fun PlatformNotFoundData.toDomain(): PlatformNotFound = PlatformNotFound(platformId)

internal fun ProjectNotFoundData.toDomain(): ProjectNotFound = ProjectNotFound(projectId from platformId)
