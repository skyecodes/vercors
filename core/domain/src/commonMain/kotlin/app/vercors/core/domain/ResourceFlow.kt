package app.vercors.core.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*

typealias ResourceFlow<T> = Flow<Resource<T>>

fun <T> ResourceFlow(supplier: suspend () -> T): ResourceFlow<T> = flow {
    emit(Resource.Loading())
    emit(Resource.Loaded(Result(supplier)))
}

fun <T> ResourceFlowResult(supplier: suspend () -> Result<T>): ResourceFlow<T> = flow {
    emit(Resource.Loading())
    emit(Resource.Loaded(supplier()))
}

fun <T> ResourceFlow(supplier: Flow<Result<T>>): ResourceFlow<T> = flow {
    emit(Resource.Loading())
    supplier.onEach { emit(Resource.Loading(it)) }
        .last().let { emit(Resource.Loaded(it)) }
}

fun <T> ResourceFlow<T>.resourceStateIn(
    coroutineScope: CoroutineScope
): StateFlow<Resource<T>> = stateIn(
    scope = coroutineScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = Resource.Unloaded
)

@Deprecated("Doesn't support partial loads")
fun <T, U> ResourceFlow<T>.combineResource(other: ResourceFlow<U>): ResourceFlow<Pair<T, U>> =
    combine(other) { a, b ->
        when {
            a == Resource.Unloaded || b == Resource.Unloaded -> Resource.Unloaded
            a is Resource.Loading || b is Resource.Loading -> Resource.Loading()
            else -> try {
                Resource.Success(a.orThrow() to b.orThrow())
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                Resource.Failure(e)
            }
        }
    }
