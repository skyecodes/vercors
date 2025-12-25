package app.vercors.launcher.cli

import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import platform.posix.putenv

actual fun configureLogging() {
    KotlinLoggingConfiguration.logLevel = Level.ERROR
    putenv("KTOR_LOG_LEVEL=ERROR")
}
