package app.vercors.core.ui.state

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.StateFactoryMarker
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.vercors.core.domain.Resource
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

inline fun <T> MutableState<T>.update(block: T.() -> T) {
    value = value.block()
}

@StateFactoryMarker
fun <T> mutableResourceStateOf(
    policy: SnapshotMutationPolicy<Resource<T>> = structuralEqualityPolicy(),
): MutableState<Resource<T>> = mutableStateOf(Resource.Unloaded, policy)

@Composable
fun <T : R, R> Flow<Resource<T>>.collectResourceAsState(
    context: CoroutineContext = EmptyCoroutineContext,
): State<Resource<R>> = collectAsState(Resource.Unloaded, context)

@Composable
fun <T : R, R> Flow<Resource<T>>.collectResourceAsStateWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
): State<Resource<R>> = collectAsStateWithLifecycle(Resource.Unloaded, lifecycleOwner, minActiveState, context)
