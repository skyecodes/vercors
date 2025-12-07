package app.vercors.launcher.common.data

import io.ktor.client.plugins.cache.*
import kotlinx.io.files.Path

internal expect fun HttpCache.Config.platformHttpCache(appPath: Path)
