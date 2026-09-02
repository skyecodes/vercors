package app.vercors.feature.project.backend

import app.vercors.feature.project.api.PlatformFiles
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
    routeWithRaise(Platforms.list) {
        val result = service.getPlatformList()
        call.response.cache(1.hours)
        respond(result)
    }

    routeWithRaise(Platforms.PlatformId.get) {
        val platformId = idOf(Platforms.PlatformId)
        val result = service.getPlatformDetails(platformId)
        call.response.cache(1.hours)
        respond(result)
    }

    routeWithRaise(Platforms.PlatformId.featuredProjects) {
        val platformId = idOf(Platforms.PlatformId)
        val result = service.getFeaturedProjectList(platformId)
        call.response.cache(1.hours)
        respond(result)
    }

    routeWithRaise(Projects.search) {
        val platformId = idOf(Platforms.PlatformId)
        val result = service.search(
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
        call.response.cache(1.minutes)
        respond(result)
    }

    routeWithRaise(Projects.ProjectId.get) {
        val platformId = idOf(Platforms.PlatformId)
        val projectId = idOf(Projects.ProjectId)
        val result = service.getProjectDetails(platformId, projectId)
        call.response.cache(1.minutes)
        respond(result)
    }

    routeWithRaise(ProjectFiles.list) {
        val platformId = idOf(Platforms.PlatformId)
        val projectId = idOf(Projects.ProjectId)
        val result = service.getProjectFiles(
            platformId = platformId,
            projectId = projectId,
            gameVersion = parameters.gameVersion,
            loader = parameters.loader?.let { json.decodeFromString<ProjectLoaderData>("\"$it\"") }
        )
        call.response.cache(1.minutes)
        respond(result)
    }

    routeWithRaise(ProjectFiles.ProjectFileId.get) {
        val platformId = idOf(Platforms.PlatformId)
        val projectId = idOf(Projects.ProjectId)
        val fileId = idOf(ProjectFiles.ProjectFileId)
        val result = service.getProjectFile(platformId, projectId, fileId)
        call.response.cache(1.minutes)
        respond(result)
    }

    routeWithRaise(PlatformFiles.get) {
        val platformId = idOf(Platforms.PlatformId)
        val result = service.getFiles(platformId, parameters.fileIds)
        call.response.cache(1.minutes)
        respond(result)
    }

    routeWithRaise(ProjectFiles.ProjectFileId.download) {
        val platformId = idOf(Platforms.PlatformId)
        val projectId = idOf(Projects.ProjectId)
        val fileId = idOf(ProjectFiles.ProjectFileId)
        val url = service.getFileDownloadUrl(platformId, projectId, fileId)
        call.response.cache(1.hours)
        call.respondRedirect(url)
    }
}
