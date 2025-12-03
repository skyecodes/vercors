package app.vercors.meta.backend.project

import com.ucasoft.ktor.simpleCache.cacheOutput
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.hours

fun Route.projectRoutes() {
    val service by inject<ProjectService>()

    route("/project") {
        cacheOutput(invalidateAt = 24.hours) {
            route("/platform") {
                get {
                    call.respond(service.getPlatformList())
                }

                route("/{platformId}") {

                    get {
                        val platformId = call.pathParameters.getOrFail("platformId")
                        call.respond(service.getPlatformDetails(platformId))
                    }

                    route("/featured") {
                        get {
                            val platformId = call.pathParameters.getOrFail("platformId")
                            call.respond(service.getFeaturedProjectList(platformId))
                        }
                    }
                }
            }
            route("/type") {
                get {
                    call.respond(service.getProjectTypeList())
                }
            }

        }
    }
}