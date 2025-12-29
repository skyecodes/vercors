package app.vercors.launcher.cli

import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import platform.posix.setenv

internal actual fun configureLogging() {
    KotlinLoggingConfiguration.logLevel = Level.ERROR
    setenv("KTOR_LOG_LEVEL", "ERROR", 1)
}
