package app.vercors.core.network

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

internal actual val platformHttpClientEngineFactory: HttpClientEngineFactory<*> = CIO
