package app.vercors.meta.backend

import app.vercors.meta.backend.game.gameRoutes
import com.ucasoft.ktor.simpleCache.SimpleCache
import com.ucasoft.ktor.simpleMemoryCache.memoryCache
import com.ucasoft.ktor.simpleRedisCache.redisCache
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
import org.koin.dsl.module
import org.koin.environmentProperties
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.koin

lateinit var scope: CoroutineScope

fun main() {
    scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
    scope.cancel()
}

fun Application.module() {
    install(Koin) {
        modules(
            MetaBackModule().module,
            module {
                single<CoroutineScope> { scope }
            })
        environmentProperties()
    }

    install(ContentNegotiation) {
        json()
    }

    install(SimpleCache) {
        val cacheHost = koin().getProperty<String>("CACHE_HOST")
        if (cacheHost != null) {
            redisCache {
                host = cacheHost
                port = koin().getProperty("CACHE_PORT") ?: 6379
            }
        } else {
            memoryCache {}
        }
    }

    routing {
        route("/v1") {
            gameRoutes()
        }
    }
}