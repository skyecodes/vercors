package app.vercors.launcher.core.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class MviViewModel<State : Any, Intent : Any, Effect : Any>(
    private val defaultState: State,
    started: SharingStarted = SharingStarted.WhileSubscribed(3000)
) : ViewModel() {
    protected val logger = KotlinLogging.logger(this::class.qualifiedName!!)

    private val _state: MutableStateFlow<State> = MutableStateFlow(defaultState)
    val state: StateFlow<State> = _state
        .onStart { onStart() }
        .onEach { onStateUpdate(it) }
        .onCompletion { onStop(it) }
        .stateIn(viewModelScope, started, defaultState)

    private val _effects: Channel<Effect> = Channel(capacity = Channel.CONFLATED)
    val effects: Flow<Effect> = _effects.receiveAsFlow().onEach { onEffect(it) }

    fun onIntent(intent: Intent) {
        logger.debug { "Handling Intent: ${intent::class.simpleName}" }
        logger.trace { "Intent data: $intent" }
        updateState(state.value.reduce(intent))
    }

    protected open fun onStart() {
        logger.debug { "Starting" }
    }

    protected open fun onStop(cause: Throwable?) {
        logger.debug { "Stopping" }
    }

    protected open fun onStateUpdate(state: State) {
        logger.debug { "State updated: ${state::class.simpleName}" }
        logger.trace { "State data: $state" }
    }

    protected open fun onEffect(effect: Effect) {
        logger.debug { "Effect produced: ${effect::class.simpleName}" }
        logger.trace { "Effect data: $effect" }
    }

    protected fun resetState() {
        updateState(defaultState)
    }

    protected fun updateState(state: State) {
        _state.tryEmit(state)
    }

    protected fun produceEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    protected fun <T> collectInScope(flow: Flow<T>, collector: FlowCollector<T>) =
        runInScope { flow.collect(collector) }

    protected fun runInScope(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch { block() }

    /**
     * Take the current state and an intent.
     * Returns the updated state or null if it shouldn't be updated.
     */
    protected abstract fun State.reduce(intent: Intent): State

    protected fun State.withEffect(vararg effects: Effect): State = also { effects.forEach { produceEffect(it) } }
}

