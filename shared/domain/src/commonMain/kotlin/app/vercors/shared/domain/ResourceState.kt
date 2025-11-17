package app.vercors.shared.domain

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val logger = KotlinLogging.logger {}

typealias MutableResourceStateFlow<T> = MutableStateFlow<Resource<T>>

typealias ResourceStateFlow<T> = StateFlow<Resource<T>>

fun <T> MutableResourceStateFlow(initialValue: T? = null): MutableResourceStateFlow<T> =
    MutableStateFlow(initialValue?.let { Resource.Loaded(Result.Success(it)) } ?: Resource.Unloaded)

fun <T> MutableResourceStateFlow<T>.asResourceStateFlow(
    coroutineScope: CoroutineScope,
    onStart: suspend FlowCollector<Resource<T>>.() -> Unit = {},
    onEach: suspend (Resource<T>) -> Unit = {},
    onCompletion: suspend FlowCollector<Resource<T>>.(cause: Throwable?) -> Unit = {},
): ResourceStateFlow<T> = onStart(onStart)
    .onEach(onEach)
    .onCompletion(onCompletion)
    .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), value)

suspend fun <T> MutableResourceStateFlow<T>.load(
    mutex: Mutex,
    loader: suspend () -> T
) {
    if (mutex.isLocked) return
    mutex.withLock {
        emit(Resource.Loading)
        supervisorScope {
            logger.debug { "Loading state" }
            val result = try {
                val data = loader()
                currentCoroutineContext().ensureActive()
                Result.Success(data)
            } catch (e: Exception) {
                try {
                    currentCoroutineContext().ensureActive()
                    Result.Error(e)
                } catch (e2: Exception) {
                    Result.Error(e2)
                }
            }
            emit(Resource.Loaded(result))
            logger.debug { "State loaded" }
        }
    }
}

suspend fun <T> MutableResourceStateFlow<T>.update(
    mutex: Mutex,
    update: suspend (T) -> Unit
) {
    mutex.withLock { getValueOrNull()?.let { update(it) } }
}

fun <T> ResourceStateFlow<T>.getValueOrNull(): T? = value.getOrNull()

fun <T> ResourceStateFlow<T>.getValueOrThrow(): T = value.getOrThrow()

suspend fun <T> ResourceStateFlow<T>.awaitLoaded(): Resource.Loaded<T> = filterIsInstance<Resource.Loaded<T>>().first()

suspend fun <T> ResourceStateFlow<T>.awaitValueOrNull(): T? = awaitLoaded().getOrNull()

suspend fun <T> ResourceStateFlow<T>.awaitValueOrThrow(): T = awaitLoaded().getOrThrow()

fun <T> MutableResourceStateFlow<T>.updateMap(mapper: (T) -> T) =
    update { res -> res.map(mapper) }

/**
 * Remove a specific element from the list
 *
 * @param item  the item to remove
 * @param eq    the equality condition
 */
fun <T> MutableResourceStateFlow<List<T>>.removeFromList(item: T, eq: (a: T, b: T) -> Boolean) =
    updateMap { list -> list.filter { !eq(it, item) } }

/**
 * Add an item to the list or updates it if it already exists in the list
 *
 * @param item  the item to add or update
 * @param eq    the equality condition
 */
fun <T> MutableResourceStateFlow<List<T>>.addToOrUpdateList(item: T, eq: (a: T, b: T) -> Boolean) =
    updateMap { list ->
        if (list.any { eq(it, item) })
            list.map { if (eq(it, item)) item else it }
        else
            list + item
    }