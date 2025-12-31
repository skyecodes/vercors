package app.vercors.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.jvm.JvmInline

sealed interface Resource<out T> {
    data object Unloaded : Resource<Nothing>

    @JvmInline
    value class Loading<out T>(val result: Result<T>? = null) : Resource<T>

    @JvmInline
    value class Loaded<out T>(val result: Result<T>) : Resource<T>

    companion object {
        fun <T> Success(data: T): Resource<T> = Loaded(Result.Success(data))

        fun Failure(error: Exception): Resource<Nothing> = Loaded(Result.Failure(error))

        operator fun <T> Companion.invoke(supplier: suspend () -> T): Flow<Resource<T>> = flow {
            emit(Loading())
            emit(Loaded(Result(supplier)))
        }
    }
}

inline val Resource<*>.isUnloaded get() = this is Resource.Unloaded

inline val Resource<*>.isLoading get() = this is Resource.Loading

inline val Resource<*>.isLoaded get() = this is Resource.Loaded

inline val Resource<*>.isSuccess get() = this is Resource.Loaded && this.result.isSuccess

inline val Resource<*>.isFailure get() = this is Resource.Loaded && this.result.isFailure

fun <T> Resource<T>.orNull(): T? = when (this) {
    is Resource.Loaded -> result.orNull()
    is Resource.Loading -> result?.orNull()
    Resource.Unloaded -> null
}

fun <T> Resource<T>.orThrow(): T = when (this) {
    is Resource.Loaded -> result.orThrow()
    is Resource.Loading -> result?.orThrow() ?: throw NotFinishedLoadingException()
    Resource.Unloaded -> throw NotLoadedException()
}

fun <T> Resource<T>.orElse(defaultValue: T): T = when (this) {
    is Resource.Loaded -> result.orElse(defaultValue)
    is Resource.Loading -> result?.orElse(defaultValue) ?: defaultValue
    Resource.Unloaded -> defaultValue
}

inline fun <T> Resource<T>.orElse(supplier: () -> T): T = when (this) {
    is Resource.Loaded -> result.orElse(supplier)
    is Resource.Loading -> result?.orElse(supplier) ?: supplier()
    Resource.Unloaded -> supplier()
}

fun <T> Resource<T>.resultOrNull(): Result<T>? = when (this) {
    is Resource.Loaded -> result
    is Resource.Loading -> result
    Resource.Unloaded -> null
}

inline fun <T> Resource<T>.onSuccess(block: (T) -> Unit): Resource<T> = apply {
    if (this is Resource.Loaded) result.onSuccess(block)
}

inline fun <T> Resource<T>.onFailure(block: (Exception) -> Unit): Resource<T> = apply {
    if (this is Resource.Loaded) result.onFailure(block)
}

inline fun <T, U> Resource<T>.map(transform: (T) -> U): Resource<U> = when (this) {
    is Resource.Loaded -> Resource.Loaded(result.map(transform))
    is Resource.Loading -> Resource.Loading(result?.map(transform))
    Resource.Unloaded -> Resource.Unloaded
}

sealed class ResourceException(message: String) : Exception(message)

class NotFinishedLoadingException : ResourceException("Resource is not finished loading")

class NotLoadedException : ResourceException("Resource is not loaded")
