package app.vercors.shared.domain

import app.vercors.shared.domain.Resource.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.jvm.JvmInline

sealed interface Resource<out T> {
    object Unloaded : Resource<Nothing>
    object Loading : Resource<Nothing>

    @JvmInline
    value class Loaded<out T>(val result: Result<T>) : Resource<T>

    companion object {
        fun <T> Success(data: T): Resource<T> = Loaded(Result.Success(data))
        fun Error(exception: Exception): Resource<Nothing> = Loaded(Result.Error(exception))
    }
}

inline fun <T, U> Resource<T>.map(transform: (T) -> U): Resource<U> = when (this) {
    Unloaded -> Unloaded
    Loading -> Loading
    is Loaded -> Loaded(result.map(transform))
}

fun <T> Resource<T>.getOrNull(): T? = when (this) {
    is Loaded -> result.getOrNull()
    else -> null
}

fun <T> Resource<T>.getOrThrow(): T = when (this) {
    is Loaded -> result.getOrThrow()
    Unloaded -> error("Resource is uninitialized")
    Loading -> error("Resource is loading")
}

fun <T, U> Flow<Resource<T>>.combineResource(other: Flow<Resource<U>>): Flow<Resource<Pair<T, U>>> =
    combine(other) { a, b ->
        when {
            a == Unloaded || b == Unloaded -> Unloaded
            a == Loading || b == Loading -> Loading
            else -> try {
                Resource.Success(a.getOrThrow() to b.getOrThrow())
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                Resource.Error(e)
            }
        }
    }
