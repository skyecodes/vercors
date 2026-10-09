package app.vercors.core.network

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@Inject
class MetaApiConnectivity(
    @Named(META_HEALTH_CHECK_HTTP_CLIENT) private val httpClient: HttpClient,
    coroutineScope: CoroutineScope,
) {
    private val _isReachable = MutableStateFlow(true)
    val isReachable: StateFlow<Boolean> = _isReachable.asStateFlow()

    init {
        coroutineScope.launch {
            while (true) {
                val reachable = try {
                    httpClient.get("health").status.isSuccess()
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    false
                }
                if (reachable != _isReachable.value) {
                    logger.info { "Meta API reachability changed: $reachable" }
                }
                _isReachable.value = reachable
                delay(10.seconds)
            }
        }
    }

    context(_: Raise<RemoteError>)
    suspend fun <T> request(callProducer: suspend () -> T): T {
        ensure(isReachable.value) { RemoteError.Network }
        return makeRequest(callProducer)
    }
}
