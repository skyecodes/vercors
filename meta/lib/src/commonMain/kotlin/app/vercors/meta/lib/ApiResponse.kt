package app.vercors.meta.lib

import app.vercors.shared.domain.Result
import kotlin.jvm.JvmInline

sealed interface ApiResponse<out T> {
    @JvmInline
    value class Success<out T>(val data: T) : ApiResponse<T>
    sealed interface Error : ApiResponse<Nothing> {
        val throwable: Throwable

        sealed interface Http : Error {
            val status: Int
            val details: ApiError?

            data class ClientError(
                override val status: Int,
                override val details: ApiError?,
                override val throwable: Throwable
            ) : Http

            data class ServerError(
                override val status: Int,
                override val details: ApiError?,
                override val throwable: Throwable
            ) : Http
        }

        data class NetworkError(override val throwable: Throwable) : Error
        data class SerializationError(override val throwable: Throwable) : Error
        data class UnknownError(override val throwable: Throwable) : Error
    }

    fun toResult(): Result<T> = when (this) {
        is Success -> Result.Success(data)
        is Error -> Result.Error(throwable)
    }

    fun <U> map(mapper: (T) -> U): ApiResponse<U> = when (this) {
        is Success -> Success(mapper(data))
        is Error -> this
    }
}
