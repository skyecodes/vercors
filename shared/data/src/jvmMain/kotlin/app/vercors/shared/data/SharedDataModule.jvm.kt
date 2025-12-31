package app.vercors.shared.data

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

internal actual val platformHttpClientEngineFactory: HttpClientEngineFactory<*> = CIO
