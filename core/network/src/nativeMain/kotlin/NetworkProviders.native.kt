package app.vercors.core.network

import io.ktor.client.engine.*
import io.ktor.client.engine.curl.*

internal actual val platformHttpClientEngineFactory: HttpClientEngineFactory<*> = Curl
