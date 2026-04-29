package app.vercors.launcher.cli

import io.github.oshai.kotlinlogging.DirectLoggerFactory
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import kotlinx.io.files.Path

actual fun configureLoggingPre() {
    KotlinLoggingConfiguration.loggerFactory = DirectLoggerFactory
    KotlinLoggingConfiguration.direct.logLevel = Level.TRACE
}

actual fun configureLoggingPost(logDir: Path) {
    // Do nothing
}
