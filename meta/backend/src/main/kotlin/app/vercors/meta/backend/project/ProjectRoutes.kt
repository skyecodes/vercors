package app.vercors.meta.backend.project

import app.vercors.meta.backend.cache
import app.vercors.meta.backend.getValue
import app.vercors.meta.lib.project.ProjectTypeData
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun Route.projectRoutes() {
    val service by inject<ProjectService>()

    route("/platform") {
        getPlatformList(service)

        route("/{platformId}") {
            getPlatformDetails(service)

            route("/featured-project") {
                getFeaturedProjectList(service)
            }

            route("/project") {
                getProjectList(service)

                route("/{projectId}") {
                    getProjectDetails(service)
                }
            }
        }
    }
}

private fun Route.getPlatformList(service: ProjectService) {
    cache(24.hours) {
        get {
            call.respond(service.getPlatformList())
        }
    }
}

private fun Route.getPlatformDetails(service: ProjectService) {
    cache(24.hours) {
        get {
            val platformId by call.pathParameters
            call.respond(service.getPlatformDetails(platformId))
        }
    }
}

private fun Route.getFeaturedProjectList(service: ProjectService) {
    cache(24.hours) {
        get {
            val platformId by call.pathParameters
            call.respond(service.getFeaturedProjectList(platformId))
        }
    }
}

private fun Route.getProjectList(service: ProjectService) {
    cache(1.minutes) {
        get {
            val json by inject<Json>()
            val platformId by call.pathParameters
            val type = call.queryParameters["type"]
            val categories = call.queryParameters.getAll("categories")
            val query = call.queryParameters["query"]
            val pageSize = call.queryParameters["pageSize"]?.toIntOrNull()
            call.respond(
                service.search(
                    platformId = platformId,
                    projectType = type?.let { json.decodeFromString<ProjectTypeData>(""""$it"""") },
                    categories = categories,
                    query = query,
                    pageSize = pageSize
                )
            )
        }
    }
}

private fun Route.getProjectDetails(service: ProjectService) {
    cache(1.minutes) {
        get {
            val platformId by call.pathParameters
            val projectId by call.pathParameters
            call.respond(service.getProjectDetails(platformId, projectId))
        }
    }
}
