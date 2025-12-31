package app.vercors.meta.backend.game

import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.gameRoutes() {
    route("/game") {
        get("/version") {
            call.respondRedirect("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json", true)
        }

        get("/java") {
            call.respondRedirect(
                "https://piston-meta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json",
                true
            )
        }
    }
}
