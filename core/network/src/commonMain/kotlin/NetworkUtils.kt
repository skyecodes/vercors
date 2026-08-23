package app.vercors.core.network

import arrow.core.raise.Raise
import arrow.core.raise.catch
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.serialization.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.serialization.SerializationException
import opensavvy.spine.client.SpineReceptionException

private val logger = KotlinLogging.logger {}

suspend fun HttpResponse.bodyAsSource(): Source = bodyAsChannel().asSource().buffered()

/**
 * Platform-specific extra classification for [isNetworkUnreachable] — e.g. the JVM/CIO engine throws
 * `java.nio.channels.UnresolvedAddressException` (an `IllegalArgumentException`, not an `IOException`) when
 * DNS resolution fails locally, before any connection is even attempted.
 */
expect fun Throwable.isPlatformNetworkUnreachable(): Boolean

/**
 * True for exceptions that mean the request never reached the server (connection refused/unreachable,
 * DNS failure, timeout at the socket level) rather than a real HTTP response or an unrelated logic error.
 * Kotlin/Native's Curl HTTP engine reports these as a plain IllegalStateException instead of an IOException
 * subtype — see io.ktor.client.engine.curl.internal.CurlMultiApiHandler.collectFailedResponse. Explicitly
 * excludes CancellationException (a JVM IllegalStateException subtype) so callers outside Arrow's own
 * cancellation-safe `catch()` — like AuthenticationServiceImpl — can't accidentally swallow cancellation.
 */
fun Throwable.isNetworkUnreachable(): Boolean =
    this !is CancellationException &&
            (this is IOException || this is IllegalStateException || isPlatformNetworkUnreachable())

context(err: Raise<RemoteError>)
suspend fun <T> makeRequest(callProducer: suspend () -> T): T = catch({ callProducer() }) {
    logger.error(it) { "Failed to make a network request" }
    when {
        it.isNetworkUnreachable() -> err.raise(RemoteError.Network)
        it is HttpRequestTimeoutException -> err.raise(RemoteError.Timeout)
        it is ClientRequestException -> err.raise(
            RemoteError.Http.Client(
                it.response.status.value,
                it.response.bodyAsText()
            )
        )

        it is ServerResponseException -> err.raise(
            RemoteError.Http.Server(
                it.response.status.value,
                it.response.bodyAsText()
            )
        )

        it is SerializationException || it is ContentConvertException -> err.raise(RemoteError.Serialization)
        it is SpineReceptionException -> when (val status = it.response.status.value) {
            in 400..499 -> err.raise(RemoteError.Http.Client(status, it.response.bodyAsText()))
            in 500..599 -> err.raise(RemoteError.Http.Server(status, it.response.bodyAsText()))
            else -> throw it
        }

        else -> throw it
    }
}

/**
 * Like [makeRequest], but for calls bound to the app's own meta API specifically: short-circuits to
 * [RemoteError.Network] without attempting the request at all when [MetaApiConnectivity] already knows the
 * meta API is unreachable, instead of paying the shared HttpClient's retry/backoff delay again. Must NOT be
 * used for calls to other hosts (external CDNs, Microsoft/Xbox/Mojang auth) — their reachability is
 * unrelated to the meta API's.
 */
context(err: Raise<RemoteError>)
suspend fun <T> makeMetaApiRequest(callProducer: suspend () -> T): T {
    if (!MetaApiConnectivity.isReachable.value) err.raise(RemoteError.Network)
    return makeRequest(callProducer)
}

sealed interface RemoteError {
    data object Network : RemoteError
    data object Timeout : RemoteError
    data object Serialization : RemoteError

    sealed interface Http : RemoteError {
        val status: Int
        val body: String

        data class Client(override val status: Int, override val body: String) : Http
        data class Server(override val status: Int, override val body: String) : Http
    }
}
