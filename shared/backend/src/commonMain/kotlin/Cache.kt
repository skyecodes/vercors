package app.vercors.shared.backend

import io.ktor.http.*
import io.ktor.server.response.*
import kotlin.time.Duration

fun ApplicationResponse.cache(duration: Duration) = cacheControl(
    CacheControl.MaxAge(
        maxAgeSeconds = duration.inWholeSeconds.toInt(),
        visibility = CacheControl.Visibility.Public,
    )
)
