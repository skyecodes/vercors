package app.vercors.meta.backend

import app.vercors.meta.backend.game.gameRoutes
import com.ucasoft.ktor.simpleCache.SimpleCache
import com.ucasoft.ktor.simpleRedisCache.redisCache
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import org.koin.environmentProperties
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.koin

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Koin) {
        modules(MetaBackModule().module)
        environmentProperties()
    }

    install(ContentNegotiation) {
        json()
    }

    install(SimpleCache) {
        redisCache {
            host = koin().getProperty("CACHE_HOST") ?: "localhost"
            port = koin().getProperty("CACHE_PORT") ?: 6379
        }
    }

    routing {
        route("/v1") {
            gameRoutes()
        }
    }
}