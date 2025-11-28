package app.vercors.shared.domain

import kotlinx.coroutines.CancellationException

sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(val throwable: Throwable) : Result<Nothing> {
        val isCancelled = throwable is CancellationException
    }
}

fun <T, U> Result<T>.map(transform: (T) -> U): Result<U> = when (this) {
    is Result.Success -> try {
        Result.Success(transform(data))
    } catch (e: Exception) {
        Result.Error(e)
    }

    is Result.Error -> this
}

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Error -> throw throwable
}

fun <T> runResult(block: () -> T): Result<T> = runCatching(block)
    .map { Result.Success(it) }
    .getOrElse { Result.Error(it) }

suspend fun <T> suspendRunResult(block: suspend () -> T): Result<T> = runCatching { block() }
    .map { Result.Success(it) }
    .getOrElse { Result.Error(it) }