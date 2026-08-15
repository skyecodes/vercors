package app.vercors.feature.game.backend

import app.vercors.feature.game.api.Game
import app.vercors.feature.game.api.Loaders
import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderTypeData
import app.vercors.feature.game.backend.loader.LoaderService
import app.vercors.shared.backend.cache
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import opensavvy.spine.server.arrow.routeWithRaise
import opensavvy.spine.server.respond
import opensavvy.spine.server.route
import kotlin.time.Duration.Companion.hours

fun Route.gameRoutes(loaderService: LoaderService, json: Json) {
    route(Game.versions) {
        call.respondRedirect("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json", true)
    }
    route(Game.runtimes) {
        call.respondRedirect(
            "https://piston-meta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json",
            true
        )
    }
    cache(1.hours) {
        routeWithRaise(Loaders.list) {
            val gameVersion = idOf(Game.GameVersionId)
            respond(loaderService.getAvailableLoaders(gameVersion))
        }
        routeWithRaise(Loaders.LoaderId.versions) {
            val gameVersion = idOf(Game.GameVersionId)
            val loaderType = decodeLoaderId(json, idOf(Loaders.LoaderId))
            respond(loaderService.getLoaderVersions(gameVersion, loaderType))
        }
    }
}

context(_: Raise<LoaderErrorData>)
private fun decodeLoaderId(json: Json, loaderId: String): LoaderTypeData = try {
    json.decodeFromString<LoaderTypeData>("\"$loaderId\"")
} catch (e: Exception) {
    raise(LoaderErrorData("InvalidLoader", "Unknown loader id: $loaderId"))
}
