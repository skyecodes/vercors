package app.vercors.feature.game.backend

import app.vercors.feature.game.api.Game
import io.ktor.server.response.*
import io.ktor.server.routing.*
import opensavvy.spine.server.route

fun Route.gameRoutes() {
    route(Game.versions) {
        call.respondRedirect("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json", true)
    }
    route(Game.runtimes) {
        call.respondRedirect(
            "https://piston-meta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json",
            true
        )
    }
}
