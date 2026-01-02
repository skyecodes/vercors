package app.vercors.core.domain

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> Collection<T>.toggle(value: T): List<T> = if (value in this) this - value else this + value

suspend fun <T> Iterable<T>.forEachAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    action: suspend (T) -> Unit
): Unit = coroutineScope {
    map { launch(context) { action(it) } }.joinAll()
}

suspend fun <T, R> Iterable<T>.mapAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    transform: suspend (T) -> R
): List<R> = coroutineScope {
    map { async(context) { transform(it) } }.awaitAll()
}
