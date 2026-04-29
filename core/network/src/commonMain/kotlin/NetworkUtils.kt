package app.vercors.core.network

import arrow.core.raise.Raise
import arrow.core.raise.catch
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.serialization.*
import io.ktor.utils.io.*
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.serialization.SerializationException

private val logger = KotlinLogging.logger {}

suspend fun HttpResponse.bodyAsSource(): Source = bodyAsChannel().asSource().buffered()

context(err: Raise<RemoteError>)
suspend fun <T> makeRequest(callProducer: suspend () -> T): T = catch({ callProducer() }) {
    logger.error(it) { "Failed to make a network request" }
    when (it) {
        is IOException -> err.raise(RemoteError.Network)
        is HttpRequestTimeoutException -> err.raise(RemoteError.Timeout)
        is ClientRequestException -> err.raise(
            RemoteError.Http.Client(
                it.response.status.value,
                it.response.bodyAsText()
            )
        )

        is ServerResponseException -> err.raise(
            RemoteError.Http.Server(
                it.response.status.value,
                it.response.bodyAsText()
            )
        )

        is SerializationException, is ContentConvertException -> err.raise(RemoteError.Serialization)
        else -> throw it
    }
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
