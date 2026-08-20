package app.vercors.feature.project.backend

import app.vercors.feature.project.api.Platforms
import app.vercors.feature.project.api.ProjectFiles
import app.vercors.feature.project.api.Projects
import app.vercors.feature.project.api.dto.ProjectLoaderData
import app.vercors.feature.project.api.dto.ProjectSortByData
import app.vercors.feature.project.api.dto.ProjectTypeData
import app.vercors.shared.backend.cache
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import opensavvy.spine.server.arrow.routeWithRaise
import opensavvy.spine.server.respond
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun Route.projectRoutes(service: ProjectService, json: Json) {
    cache(1.hours) {
        routeWithRaise(Platforms.list) {
            respond(service.getPlatformList())
        }
        routeWithRaise(Platforms.PlatformId.get) {
            val platformId = idOf(Platforms.PlatformId)
            respond(service.getPlatformDetails(platformId))
        }
        routeWithRaise(Platforms.PlatformId.featuredProjects) {
            val platformId = idOf(Platforms.PlatformId)
            respond(service.getFeaturedProjectList(platformId))
        }
    }
    cache(1.minutes) {
        routeWithRaise(Projects.search) {
            val platformId = idOf(Platforms.PlatformId)
            respond(
                service.search(
                    platformId = platformId,
                    projectType = parameters.type?.let { json.decodeFromString<ProjectTypeData>("\"$it\"") },
                    loaders = parameters.loaders.map { json.decodeFromString<ProjectLoaderData>("\"$it\"") }
                        .ifEmpty { null },
                    gameVersions = parameters.gameVersions.ifEmpty { null },
                    categories = parameters.categories.ifEmpty { null },
                    query = parameters.query,
                    sortBy = parameters.sortBy?.let { json.decodeFromString<ProjectSortByData>("\"$it\"") },
                    pageSize = parameters.pageSize,
                    offset = parameters.offset
                )
            )
        }
        routeWithRaise(Projects.ProjectId.get) {
            val platformId = idOf(Platforms.PlatformId)
            val projectId = idOf(Projects.ProjectId)
            respond(service.getProjectDetails(platformId, projectId))
        }
        routeWithRaise(ProjectFiles.list) {
            val platformId = idOf(Platforms.PlatformId)
            val projectId = idOf(Projects.ProjectId)
            respond(
                service.getProjectFiles(
                    platformId = platformId,
                    projectId = projectId,
                    gameVersion = parameters.gameVersion,
                    loader = parameters.loader?.let { json.decodeFromString<ProjectLoaderData>("\"$it\"") }
                )
            )
        }
        routeWithRaise(ProjectFiles.ProjectFileId.get) {
            val platformId = idOf(Platforms.PlatformId)
            val projectId = idOf(Projects.ProjectId)
            val fileId = idOf(ProjectFiles.ProjectFileId)
            respond(service.getProjectFile(platformId, projectId, fileId))
        }
    }
    // Not cached: the CurseForge download-url resolution can return short-lived/signed URLs.
    routeWithRaise(ProjectFiles.ProjectFileId.download) {
        val platformId = idOf(Platforms.PlatformId)
        val projectId = idOf(Projects.ProjectId)
        val fileId = idOf(ProjectFiles.ProjectFileId)
        val url = service.getFileDownloadUrl(platformId, projectId, fileId)
        call.respondRedirect(url)
    }
}
