package app.vercors.launcher.common.data

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import kotlinx.io.files.Path
import java.io.File

internal actual fun HttpCache.Config.platformHttpCache(appPath: Path) {
    publicStorage(
        FileStorage(
            directory = File(Path(appPath, "cache", "ktor").toString())
        )
    )
}

internal actual val platformHttpClientEngineFactory: HttpClientEngineFactory<*> = CIO
