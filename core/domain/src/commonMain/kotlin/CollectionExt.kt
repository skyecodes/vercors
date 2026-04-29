package app.vercors.core.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> Collection<T>.toggle(value: T): List<T> = if (value in this) this - value else this + value

suspend fun <T> Iterable<T>.forEachAsync(
    concurrency: Int,
    context: CoroutineContext = EmptyCoroutineContext,
    action: suspend CoroutineScope.(T) -> Unit
) {
    val semaphore = Semaphore(concurrency)
    return forEachAsync(context) {
        semaphore.withPermit { action(it) }
    }
}

suspend fun <T> Iterable<T>.forEachAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    action: suspend CoroutineScope.(T) -> Unit
): Unit = coroutineScope {
    map { launch(context) { action(it) } }.joinAll()
}

suspend fun <T, R> Iterable<T>.mapAsync(
    concurrency: Int,
    context: CoroutineContext = EmptyCoroutineContext,
    transform: suspend CoroutineScope.(T) -> R
): List<R> {
    val semaphore = Semaphore(concurrency)
    return mapAsync(context) {
        semaphore.withPermit { transform(it) }
    }
}

suspend fun <T, R> Iterable<T>.mapAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    transform: suspend CoroutineScope.(T) -> R
): List<R> = coroutineScope {
    map { async(context) { transform(it) } }.awaitAll()
}
