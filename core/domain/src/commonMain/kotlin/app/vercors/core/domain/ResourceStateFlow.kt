package app.vercors.core.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias ResourceStateFlow<T> = StateFlow<Resource<T>>

typealias MutableResourceStateFlow<T> = MutableStateFlow<Resource<T>>

fun <T> MutableResourceStateFlow(initialValue: T? = null): MutableResourceStateFlow<T> =
    MutableStateFlow(initialValue?.let { Resource.Success(it) } ?: Resource.Unloaded)

fun <T> MutableResourceStateFlow<T>.asResourceStateFlow(
    coroutineScope: CoroutineScope,
    onStart: (suspend FlowCollector<Resource<T>>.() -> Unit)? = null,
    onEach: (suspend (Resource<T>) -> Unit)? = null,
    onCompletion: (suspend FlowCollector<Resource<T>>.(Throwable?) -> Unit)? = null,
    started: SharingStarted = SharingStarted.WhileSubscribed(5000),
    initialValue: Resource<T> = value
): ResourceStateFlow<T> =
    runIfNotNull(onStart) { onStart(it) }
        .runIfNotNull(onEach) { onEach(it) }
        .runIfNotNull(onCompletion) { onCompletion(it) }
        .stateIn(coroutineScope, started, initialValue)

private inline fun <T : U, U, X> T.runIfNotNull(value: X?, block: T.(X) -> U): U =
    value?.let { block(it) } ?: this

suspend inline fun <T> MutableResourceStateFlow<T>.load(mutex: Mutex, supplier: suspend () -> T) {
    if (!mutex.isLocked)
        mutex.withLock { loadUnsafe(supplier) }
}

suspend inline fun <T> MutableResourceStateFlow<T>.loadUnsafe(supplier: suspend () -> T) {
    emit(Resource.Loading(value.resultOrNull()))
    emit(Resource.Loaded(Result(supplier)))
}

suspend inline fun <T> MutableResourceStateFlow<T>.read(mutex: Mutex, consumer: suspend (T) -> Unit) = mutex.withLock {
    consumer(value.orThrow())
}

suspend inline fun <T> ResourceFlow<T>.await(): Result<T> = filterIsInstance<Resource.Loaded<T>>().first().result

inline fun <T> MutableResourceStateFlow<T>.updateMap(mapper: (T) -> T) =
    update { res -> res.map(mapper) }

/**
 * Remove a specific element from the list
 *
 * @param item  the item to remove
 * @param eq    the equality condition
 */
inline fun <T> MutableResourceStateFlow<List<T>>.removeFromList(item: T, eq: (a: T, b: T) -> Boolean) =
    updateMap { list -> list.filter { !eq(it, item) } }

/**
 * Add an item to the list or updates it if it already exists in the list
 *
 * @param item  the item to add or update
 * @param eq    the equality condition
 */
inline fun <T> MutableResourceStateFlow<List<T>>.addToOrUpdateList(item: T, eq: (a: T, b: T) -> Boolean) =
    updateMap { list ->
        if (list.any { eq(it, item) })
            list.map { if (eq(it, item)) item else it }
        else
            list + item
    }
