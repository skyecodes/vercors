package app.vercors.core.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.ProgressiveOutcome

typealias ProgressiveStateFlow<Failure, Value> = StateFlow<ProgressiveOutcome<Failure, Value>>

typealias MutableProgressiveStateFlow<Failure, Value> = MutableStateFlow<ProgressiveOutcome<Failure, Value>>

fun <Failure, Value> MutableProgressiveStateFlow(
    initial: ProgressiveOutcome<Failure, Value> = ProgressiveOutcome.Incomplete()
): MutableProgressiveStateFlow<Failure, Value> = MutableStateFlow(initial)

fun <Failure, Value> ProgressiveFlow<Failure, Value>.progressiveStateIn(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed(5000),
    initialValue: ProgressiveOutcome.Incomplete = ProgressiveOutcome.Incomplete()
): ProgressiveStateFlow<Failure, Value> = stateIn(scope, started, initialValue)
