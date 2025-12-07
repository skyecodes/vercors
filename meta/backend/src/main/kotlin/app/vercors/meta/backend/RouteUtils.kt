package app.vercors.meta.backend

import com.ucasoft.ktor.simpleCache.cacheOutput
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlin.reflect.KProperty
import kotlin.time.Duration

operator fun StringValues.getValue(thisRef: Any?, property: KProperty<*>): String = get(property.name)!!

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
