package app.vercors.meta.client

import app.vercors.core.domain.Result
import app.vercors.meta.lib.ApiError
import kotlin.jvm.JvmInline

sealed interface ApiResponse<out T> {
    @JvmInline
    value class Success<out T>(val data: T) : ApiResponse<T>
    sealed interface Failure : ApiResponse<Nothing> {
        val error: Exception

        sealed interface Http : Failure {
            val status: Int
            val details: ApiError?

            data class ClientError(
                override val status: Int,
                override val details: ApiError?,
                override val error: Exception
            ) : Http

            data class ServerError(
                override val status: Int,
                override val details: ApiError?,
                override val error: Exception
            ) : Http
        }

        data class NetworkError(override val error: Exception) : Failure
        data class SerializationError(override val error: Exception) : Failure
        data class UnknownError(override val error: Exception) : Failure
    }

    fun toResult(): Result<T> = when (this) {
        is Success -> Result.Success(data)
        is Failure -> Result.Failure(error)
    }

    fun <U> map(mapper: (T) -> U): ApiResponse<U> = when (this) {
        is Success -> Success(mapper(data))
        is Failure -> this
    }
}
