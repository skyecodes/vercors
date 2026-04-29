package app.vercors.launcher.cli

import kotlinx.io.files.Path

actual fun configureLoggingPre() {
    // Do nothing
}

actual fun configureLoggingPost(logDir: Path) {
    System.setProperty("app.vercors.logDir", logDir.toString())
}
