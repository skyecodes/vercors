package app.vercors.launcher.cli

import platform.posix.setenv

actual fun configureLogging() {
    // logLevel is not accessible on macosMain - https://github.com/oshai/kotlin-logging/issues/494
    setenv("KTOR_LOG_LEVEL", "ERROR", 1)
}
