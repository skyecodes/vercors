package app.vercors.launcher.cli

actual fun configureLogging() {
    // KotlinLoggingConfiguration is not accessible on nativeMain - https://github.com/oshai/kotlin-logging/issues/494
    // TODO: Find a workaround for this
}