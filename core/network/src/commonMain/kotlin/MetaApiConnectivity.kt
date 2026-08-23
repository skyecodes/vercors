package app.vercors.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks whether the app's own meta API was reachable as of the last health-check poll ([MetaApiConnectivityPoller]).
 * Starts optimistic (true) since no poll has run yet at app startup.
 */
object MetaApiConnectivity {
    private val _isReachable = MutableStateFlow(true)
    val isReachable: StateFlow<Boolean> = _isReachable.asStateFlow()

    internal fun update(reachable: Boolean) {
        _isReachable.value = reachable
    }
}
