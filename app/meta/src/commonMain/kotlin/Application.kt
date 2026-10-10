package app.vercors.meta

import app.vercors.feature.account.backend.accountRoutes
import app.vercors.feature.game.backend.gameRoutes
import app.vercors.feature.game.backend.loader.LoaderService
import app.vercors.feature.project.backend.ProjectService
import app.vercors.feature.project.backend.projectRoutes
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    val appGraph = createGraph<AppGraph>()
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = { module(appGraph) })
        .start(wait = true)
}

internal fun Application.module(appGraph: AppGraph) {
    install(ContentNegotiation) {
        json(Json {
            explicitNulls = false
        })
    }

    routing {
        get("/health") { call.respond(HttpStatusCode.OK) }
        gameRoutes(appGraph.loaderService, appGraph.json)
        projectRoutes(appGraph.projectService, appGraph.json)
        accountRoutes()
    }
}

@DependencyGraph(AppScope::class)
internal interface AppGraph {
    val projectService: ProjectService
    val loaderService: LoaderService
    val json: Json
}
