package app.vercors.core.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

/**
 * Periodically pings the meta API's `/health` endpoint and updates [MetaApiConnectivity] so meta-API-bound
 * calls ([makeMetaApiRequest]) can skip the network attempt entirely once the backend is known-unreachable,
 * instead of paying the shared HttpClient's retry/backoff delay on every single call.
 */
@SingleIn(AppScope::class)
@Inject
class MetaApiConnectivityPoller(
    @Named("metaHealthCheckHttpClient") private val httpClient: HttpClient,
    coroutineScope: CoroutineScope,
) {
    init {
        coroutineScope.launch {
            while (true) {
                val reachable = try {
                    httpClient.get("health").status.isSuccess()
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    false
                }
                if (reachable != MetaApiConnectivity.isReachable.value) {
                    logger.info { "Meta API reachability changed: $reachable" }
                }
                MetaApiConnectivity.update(reachable)
                delay(10.seconds)
            }
        }
    }
}
