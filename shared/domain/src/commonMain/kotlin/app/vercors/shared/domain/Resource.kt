package app.vercors.shared.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface Resource<out T : Any, out E : Throwable> {
    data object Unloaded : Resource<Nothing, Nothing>
    data object Loading : Resource<Nothing, Nothing>
    sealed interface Loaded<out T : Any, out E : Throwable> : Resource<T, E>
    data class Error<E : Throwable>(val error: E) : Resource<Nothing, E>, Loaded<Nothing, E>
    data class Success<T : Any>(val value: T) : Resource<T, Nothing>, Loaded<T, Nothing>
}

fun <T : Any, E : Throwable, U : Any> Resource<T, E>.map(mapper: (T) -> U): Resource<U, E> = when (this) {
    is Resource.Success -> Resource.Success(mapper(value))
    is Resource.Error -> this
    Resource.Unloaded -> Resource.Unloaded
    Resource.Loading -> Resource.Loading
}


typealias MutableResourceStateFlow<T, E> = MutableStateFlow<Resource<T, E>>

typealias ResourceStateFlow<T, E> = StateFlow<Resource<T, E>>

typealias ResourceFlow<T, E> = Flow<Resource<T, E>>

typealias MutableResourceListStateFlow<T, E> = MutableResourceStateFlow<List<T>, E>

typealias ResourceListStateFlow<T, E> = ResourceStateFlow<List<T>, E>

typealias ResourceListFlow<T, E> = ResourceFlow<List<T>, E>

fun <T : Any, E : Throwable> MutableResourceStateFlow(initialValue: Resource<T, E> = Resource.Unloaded): MutableResourceStateFlow<T, E> =
    MutableStateFlow<Resource<T, E>>(initialValue)

fun <T : Any, E : Throwable> MutableResourceListStateFlow(initialValue: Resource<List<T>, E> = Resource.Unloaded): MutableResourceListStateFlow<T, E> =
    MutableStateFlow<Resource<List<T>, E>>(initialValue)

suspend fun <T : Any> MutableResourceStateFlow<T, *>.load(
    mutex: Mutex,
    loader: suspend () -> T
) = mutex.withLock {
    if (value != Resource.Loading) {
        supervisorScope {
            emit(Resource.Loading)
            try {
                emit(Resource.Success(loader()))
            } catch (e: Exception) {
                emit(Resource.Error(e))
            }
        }
    }
}

suspend fun <T : Any> ResourceStateFlow<T, *>.collectValues(collector: (T) -> Unit) =
    filterIsInstance<Resource.Success<T>>().collect { collector(it.value) }

suspend fun <T : Any, E : Throwable> ResourceStateFlow<T, E>.awaitValueOrThrow(): T =
    filterIsInstance<Resource.Loaded<T, E>>()
        .map {
            when (it) {
                is Resource.Success<T> -> it.value
                is Resource.Error<E> -> throw it.error
            }
        }
        .first()

fun <T : Any, E : Throwable> MutableResourceStateFlow<T, E>.updateMap(mapper: (T) -> T) =
    update { res -> res.map(mapper) }

fun <T : Any> ResourceFlow<T, *>.asResourceStateFlow(
    coroutineScope: CoroutineScope,
    loadImmediately: Boolean = true,
    load: suspend () -> Unit
): ResourceStateFlow<T, *> {
    val flow = if (loadImmediately) apply { coroutineScope.launch { load() } } else onStart { load() }
    return flow.distinctUntilChanged().stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000L), Resource.Unloaded)
}


/**
 * Remove a specific element from the list
 *
 * @param item  the item to remove
 * @param eq    the equality condition
 */
fun <T> MutableResourceStateFlow<List<T>, *>.removeFromList(item: T, eq: (a: T, b: T) -> Boolean) =
    updateMap { list -> list.filter { !eq(it, item) } }

/**
 * Add an item to the list or updates it if it already exists in the list
 *
 * @param item  the item to add or update
 * @param eq    the equality condition
 */
fun <T> MutableResourceStateFlow<List<T>, *>.addToOrUpdateList(item: T, eq: (a: T, b: T) -> Boolean) =
    updateMap { list ->
        if (list.any { eq(it, item) })
            list.map { if (eq(it, item)) item else it }
        else
            list + item
    }