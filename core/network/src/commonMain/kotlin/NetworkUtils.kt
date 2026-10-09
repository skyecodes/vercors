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

expect fun Throwable.isPlatformNetworkUnreachable(): Boolean

fun Throwable.isNetworkUnreachable(): Boolean =
    this !is CancellationException && this !is ResponseException &&
            (this is IOException || this is IllegalStateException || isPlatformNetworkUnreachable())

context(err: Raise<RemoteError>)
suspend fun <T> makeRequest(callProducer: suspend () -> T): T = catch({ callProducer() }) {
    logger.error(it) { "Failed to make a network request" }
    when {
        it.isNetworkUnreachable() -> err.raise(RemoteError.Network)
        it is HttpRequestTimeoutException -> err.raise(RemoteError.Timeout)
        it is ResponseException -> err.raise(convertToRemoteError(it.response))
        it is SerializationException || it is ContentConvertException -> err.raise(RemoteError.Serialization)
        it is SpineReceptionException -> err.raise(convertToRemoteError(it.response))

        else -> throw it
    }
}

private suspend fun convertToRemoteError(response: HttpResponse) =
    when (val status = response.status.value) {
        in 300..399 -> RemoteError.Http.Redirect(status, response.bodyAsText())
        in 400..499 -> RemoteError.Http.Client(status, response.bodyAsText())
        in 500..599 -> RemoteError.Http.Server(status, response.bodyAsText())
        else -> RemoteError.Http.Other(status, response.bodyAsText())
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
        data class Redirect(override val status: Int, override val body: String) : Http
        data class Other(override val status: Int, override val body: String) : Http
    }
}
