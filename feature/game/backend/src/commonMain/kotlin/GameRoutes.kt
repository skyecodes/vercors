package app.vercors.feature.game.backend

import app.vercors.feature.game.api.Game
import app.vercors.feature.game.api.Loaders
import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderTypeData
import app.vercors.feature.game.backend.loader.LoaderService
import app.vercors.shared.backend.cache
import app.vercors.shared.backend.decodeEnumParameter
import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import opensavvy.spine.server.arrow.routeWithRaise
import opensavvy.spine.server.respond
import opensavvy.spine.server.route
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun Route.gameRoutes(loaderService: LoaderService, json: Json) {
    route(Game.versions) {
        call.response.cache(5.minutes)
        call.respondRedirect("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
    }
    route(Game.runtimes) {
        call.response.cache(5.minutes)
        call.respondRedirect("https://piston-meta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json")
    }
    routeWithRaise(Loaders.list) {
        val gameVersion = idOf(Game.GameVersionId)
        val result = loaderService.getAvailableLoaders(gameVersion)
        call.response.cache(1.hours)
        respond(result)
    }
    routeWithRaise(Loaders.LoaderId.versions) {
        val gameVersion = idOf(Game.GameVersionId)
        val loaderType = decodeLoaderId(json, idOf(Loaders.LoaderId))
        val result = loaderService.getLoaderVersions(gameVersion, loaderType)
        call.response.cache(5.minutes)
        respond(result)
    }
    routeWithRaise(Loaders.LoaderId.installUrl) {
        val gameVersion = idOf(Game.GameVersionId)
        val loaderType = decodeLoaderId(json, idOf(Loaders.LoaderId))
        val loaderVersion = ensureNotNull(parameters.loaderVersion) {
            LoaderErrorData("MissingParameter", "Missing required parameter: loaderVersion")
        }
        val url = loaderService.getInstallUrl(gameVersion, loaderType, loaderVersion)
        call.response.cache(1.hours)
        call.respondRedirect(url)
    }
}

context(_: Raise<LoaderErrorData>)
private fun decodeLoaderId(json: Json, loaderId: String): LoaderTypeData =
    json.decodeEnumParameter(loaderId) { LoaderErrorData("InvalidLoader", "Unknown loader id: $it") }
