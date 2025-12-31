package app.vercors.meta.client

import io.ktor.client.plugins.cache.*
import kotlinx.io.files.Path

internal actual fun HttpCache.Config.platformHttpCache(appPath: Path) {
    // Do nothing: no file cache on native target
}
