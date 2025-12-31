package app.vercors.shared.data

import io.ktor.client.engine.*
import io.ktor.client.engine.curl.*

internal actual val platformHttpClientEngineFactory: HttpClientEngineFactory<*> = Curl
