package app.vercors.meta

import app.vercors.feature.account.backend.accountRoutes
import app.vercors.feature.game.backend.gameRoutes
import app.vercors.feature.game.backend.loader.LoaderService
import app.vercors.feature.project.backend.ProjectService
import app.vercors.feature.project.backend.projectRoutes
import app.vercors.shared.backend.platformEnv
import com.ucasoft.ktor.simpleCache.SimpleCache
import com.ucasoft.ktor.simpleMemoryCache.memoryCache
import com.ucasoft.ktor.simpleRedisCache.redisCache
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json

const val VERSION = "0.1.0"

fun main() {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val appGraph = createGraphFactory<AppGraph.Factory>().create(scope)
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = { module(appGraph) })
        .start(wait = true)
    scope.cancel()
}

internal fun Application.module(appGraph: AppGraph) {
    install(ContentNegotiation) {
        json(Json {
            explicitNulls = false
        })
    }

    install(SimpleCache) {
        val cacheHost = platformEnv("CACHE_HOST")
        if (cacheHost != null) {
            redisCache {
                host = cacheHost
                port = platformEnv("CACHE_PORT")?.toIntOrNull() ?: 6379
            }
        } else {
            memoryCache {}
        }
    }

    routing {
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

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides coroutineScope: CoroutineScope): AppGraph
    }
}
