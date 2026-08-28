package app.vercors.core.network

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.curl.*

internal actual val platformHttpClientEngineFactory: HttpClientEngineFactory<*> = Curl

internal actual fun <T : HttpClientEngineConfig> HttpClientConfig<T>.installPlatformTrustStore() {
    // No-op: Curl's engine already consults the OS trust store.
}
