package app.vercors.launcher.common.data

import io.ktor.client.engine.*
import io.ktor.client.engine.curl.*
import io.ktor.client.plugins.cache.*
import kotlinx.io.files.Path

internal actual fun HttpCache.Config.platformHttpCache(appPath: Path) {
    // Do nothing: no file cache on native taregt
}

internal actual val platformHttpClientEngineFactory: HttpClientEngineFactory<*> = Curl
