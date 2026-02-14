package app.vercors.core.ui.state

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.StateFactoryMarker
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.retained.produceRetainedState
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.ProgressiveOutcome

inline fun <T> MutableState<T>.update(block: T.() -> T) {
    value = value.block()
}

@StateFactoryMarker
fun <Failure, Value> mutableProgressiveStateOf(
    policy: SnapshotMutationPolicy<ProgressiveOutcome<Failure, Value>> = structuralEqualityPolicy(),
): MutableState<ProgressiveOutcome<Failure, Value>> = mutableStateOf(ProgressiveOutcome.Incomplete(), policy)

@Composable
fun <Failure, Value> produceProgressiveState(
    vararg keys: Any?,
    initialValue: ProgressiveOutcome<Failure, Value> = ProgressiveOutcome.Incomplete(),
    producer: suspend ProduceStateScope<ProgressiveOutcome<Failure, Value>>.() -> Unit,
) = produceRetainedState(initialValue, keys = keys, producer)

@Composable
fun <Failure, Value> ProgressiveFlow<Failure, Value>.collectProgressiveAsState(
    initial: ProgressiveOutcome<Failure, Value> = ProgressiveOutcome.Incomplete()
): State<ProgressiveOutcome<Failure, Value>> = collectAsRetainedState(initial)
