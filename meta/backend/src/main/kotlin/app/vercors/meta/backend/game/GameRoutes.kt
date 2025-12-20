package app.vercors.meta.backend.game

import app.vercors.meta.backend.cache
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.minutes

fun Route.gameRoutes() {
    val service by inject<GameService>()

    route("/game") {
        cache(5.minutes) {
            route("/version") {
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
