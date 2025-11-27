package app.vercors.meta.backend.game

import com.ucasoft.ktor.simpleCache.cacheOutput
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.gameRoutes() {
    val service by inject<GameService>()

    route("/game") {
        install(CachingHeaders) {
            options { _, _ ->
                CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 300))
            }
        }

        cacheOutput {
            route("/versions") {
                get {
                    call.respond(service.getVersionList())
                }

                get("/{id}") {
                    call.respond(service.getVersionDetails(call.parameters["id"]!!))
                }
            }
        }
    }
}