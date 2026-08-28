package app.vercors.core.network

import dev.nucleusframework.nativehttp.ktor.installNativeSsl
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

internal actual val platformHttpClientEngineFactory: HttpClientEngineFactory<*> = CIO

internal actual fun <T : HttpClientEngineConfig> HttpClientConfig<T>.installPlatformTrustStore() {
    installNativeSsl()
}
