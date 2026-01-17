package app.vercors.core.domain

import app.vercors.core.domain.Result.Failure
import app.vercors.core.domain.Result.Success
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.jvm.JvmInline

sealed interface Result<out T> {
    @JvmInline
    value class Success<out T>(val data: T) : Result<T>

    @JvmInline
    value class Failure(val error: Exception) : Result<Nothing>

    companion object {
        suspend inline operator fun <T> invoke(supplier: suspend () -> T): Result<T> = try {
            Success(supplier())
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Failure(e)
        }
    }
}

inline val Result<*>.isSuccess get() = this is Success

inline val Result<*>.isFailure get() = this is Failure

fun <T> Result<T>.orNull(): T? = when (this) {
    is Success -> data
    else -> null
}

fun <T> Result<T>.orThrow(): T = when (this) {
    is Success -> data
    is Failure -> throw error
}

fun <T> Result<T>.orElse(defaultValue: T): T = when (this) {
    is Success -> data
    else -> defaultValue
}

inline fun <T> Result<T>.orElse(supplier: () -> T): T = when (this) {
    is Success -> data
    else -> supplier()
}

inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> = apply {
    if (this is Success) block(data)
}

inline fun <T> Result<T>.onFailure(block: (Exception) -> Unit): Result<T> = apply {
    if (this is Failure) block(error)
}

inline fun <T, U> Result<T>.map(transform: (T) -> U): Result<U> = when (this) {
    is Success -> Success(transform(data))
    is Failure -> this
}

inline fun <T, U> Result<T>.mapNullable(transform: (T) -> U?): Result<U>? = when (this) {
    is Success -> transform(data)?.let { Success(it) }
    is Failure -> this
}

inline fun <T, U> Result<T>.flatMap(transform: (T) -> Result<U>): Result<U> = when (this) {
    is Success -> transform(data)
    is Failure -> this
}

inline fun <T> Result<T>.fold(onSuccess: (T) -> Unit, onFailure: (Exception) -> Unit): Unit = when (this) {
    is Success -> onSuccess(data)
    is Failure -> onFailure(error)
}
