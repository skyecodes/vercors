package app.vercors.shared.ui

import androidx.compose.runtime.Stable
import com.slack.circuit.runtime.CircuitUiState

@Stable
data class Store<S, E : Event>(
    val state: S,
    val eventSink: EventSink<E>
) : CircuitUiState
