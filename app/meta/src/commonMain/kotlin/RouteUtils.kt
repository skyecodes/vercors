package app.vercors.meta

import com.ucasoft.ktor.simpleCache.cacheOutput
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.routing.*
import kotlin.time.Duration

fun Route.cache(duration: Duration, block: Route.() -> Unit) {
    cacheOutput(invalidateAt = duration) {
        install(CachingHeaders) {
            options { _, _ ->
                CachingOptions(CacheControl.MaxAge(maxAgeSeconds = duration.inWholeSeconds.toInt()))
            }
        }
        block()
    }
}
